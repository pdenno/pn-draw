(ns pdenno.pn-draw.simulate
  (:require [clojure.pprint :refer (cl-format pprint pp)]
            [gov.nist.spntools.util.reach :as pnr]
            [gov.nist.spntools.util.utils :as pnu :refer (ppprint ppp name2obj)]))

;;; Purpose: Run a PN, producing a log of its execution.

;;; In ordinary GSPN code, a marking (:queues) is just a vector of integers signifying the tokens on a place.
;;; In our implementation, we instead keep maps of queues containing tokens. Where we need the ordinary GSPN marking,
;;; we can convert this queue-base marking to it with (map count (queues-marking-order pn)).
;;; The PN is initialized with default-coloured tokens. For example where the ordinary marking might be
;;; [2 0 0 0] ours would be [[{:type :a :id 1} {:type :a :id2}] [] [] []].

(def ^:private diag (atom nil))

;;; POD When I replace next-link with the QPN equivalent, this can go away. 
(defn queues-marking-order
  "Return a vector of queues in the marking order."
  [pn]
  (let [mk (:marking-key pn)
        queues (-> pn :sim :queues)]
    (vec (map #(% queues) mk))))

(declare sim-effects pick-link step-state update-log-for-step max-tkn)
;;; Not yet a stochastic simulation, also need to implement free choice.
;;; (simulate (:pn eee) :max-steps 2)
(defn simulate
  "Run a PN for max-steps or max-token whichever comes first."
  [pn & {:keys [max-token max-steps] :or {max-token 50 max-steps 1}}] ; POD buglet: specify both
  (let [id (atom 0)]
    (as-> pn ?pn
      (pnr/renumber-pids ?pn)
      (assoc ?pn
             :sim {:log []
                   :max-tkn 1
                   :queues (zipmap
                            (:marking-key ?pn) ; POD next line will need work for colour. 
                            (map (fn [n] (vec (repeatedly n (fn [] {:jtype :blue :id (swap! id inc)}))))
                                 (:initial-marking ?pn)))})
      (reduce (fn [pn _]
                (if (>= (-> pn :sim :max-tkn) max-token) pn (sim-effects pn)))
              ?pn
              (range max-steps)))))

;;; POD: Currently I'm using next-links, because there is only one colour.
(defn sim-effects
  "Update the PN's :sim with the effects of one step."
  [pn]
  (let [marking (vec (map count (queues-marking-order pn)))
        next-links (pnr/next-links pn marking)]
    (if (empty? next-links) ; then ran out of tokens.
      pn
      (step-state pn (pick-link next-links)))))

;;; An entry in a queue looks like this: {:jtype :blue :id 4}
;;; POD currently I'm ignoring colour; specifically, I'm using next-link and not evaluating bindings.

;;; There needs to be a discipline regarding which tokens move where. It is as follows:

;;; A priority 1 to N was assigned to the N arcs out-going from a transition (that was done 
;;; in the design of the PN). Each out-going arc on the transition has a unique priority. 
;;; The priority assignments and the ids on token are used to determine what tokens
;;; will flow out of which arcs from a transition in simulation. 
;;; WILL HAVE TO ABIDE BY COLOR WHEN IMPLEMENTED.
;;; 
;;; The rules are as follows:
;;;
;;; (1) Negative balance: Tokens are removed from each in-coming place according to the
;;;     multiplicity of the arc in-coming and FIFO queueing. Among these, the OLDEST N tokens 
;;;     are removed from the PN to satisfy an imbalance of N tokens.                   
;;;     The remaining tokens are distributed so that the token requirements (multiplicity) of
;;;     the highest priority arc are satisfied first using the NEWEST remaining tokens,
;;;     then the second highest priority arc, and so on. 
;;; (2) Positive balance/Perfect balance: New tokens are created to satisfy any imbalance. 
;;;     Tokens are distributed to the places as in (1).

;;; Binding makes GP a bit more complex. In GP you can have new tokens springing up / being eliminated
;;; anywhere. I think the need for intro/elim arises out of imbalance at a transition.
;;; Thankfully, there is a constraint propagation task here. When introducing a free choice pick
;;; among binding types, one can push that through the marked graph portion until a
;;; place where there is confluence of types is reached. At that point forward arcs accept a disjunction
;;; of the types in confluence.  When there are additional free choice (like in a buffer) there is the
;;; opportunity to to reuse the old binding types or choose new ones.

;;; BTW, inhibitors can have bindings. 

(defn new-tokens
  "Create a vector of n new tokens, newest id first."
  [pn n]
  (let [tkns (->> pn :sim :queues (mapcat (fn [[_ v]] (map :id v))))
        tkns (into tkns (map :id (-> pn :sim :pulled)))   ; Some tokens could be hiding here!
        max-tkn (inc (if (empty? tkns) 0 (apply max tkns)))]
    (reduce (fn [v id] (into [{:jtype :blue :id id}] v))
            []
            (range max-tkn (+ max-tkn n)))))

;;; (1) "Negative balance: The oldest N tokens are removed to satisfy an imbalance of N tokens.
;;;     The remaining tokens are distributed so that the token requirements (multiplicity) of
;;;     the highest priority arc (lowest priority number) are satisfied first using the NEWEST
;;;     remaining tokens, then the second highest priority arc, and so on."
(defn pull-tokens
  "Collect tokens from the arcs (adjusting queues); set (-> pn :sim :pulled) to
   the tokens that will be part of push-tokens."
  [pn arcs-in]
  (as-> pn ?pn
    (assoc-in ?pn [:sim :pulled] [])
    (reduce (fn [pn arc]
              (as-> pn ?pn1
                (update-in ?pn1 [:sim :pulled]                         ; collect
                           #(into % (subvec (->> ?pn1 :sim :queues ((:source arc)))
                                          0 (:multiplicity arc))))
                (update-in ?pn1 [:sim :queues (:source arc)]           ; trim queues
                           #(subvec % (:multiplicity arc)))))
            ?pn
            arcs-in)
    (update-in ?pn [:sim :pulled] (fn [v] (vec (sort #(> (:id %1) (:id %2)) v)))))) ; NEWEST first

;;; (1) ..."The remaining tokens are distributed so that the token requirements (multiplicity) of
;;;     the highest priority arc (lowest priority number) are satisfied first using the NEWEST
;;;     remaining tokens, then the second highest priority arc, and so on."
(defn push-tokens
  "Assign tokens from (-> pn :sim :pulled) to the queues according
   to priority and multiplicity. Place the rest on (-> pn :sim :removed)."
  [pn arcs-out]
  (let [arcs-out (sort #(< (:priority %1) (:priority %2)) arcs-out)]
    (as-> pn ?pn
      (reduce (fn [pn arc]
                (let [mult (:multiplicity arc)]
                  (as-> pn ?pn2
                    ;; Update a queue according to the arc.
                    (update-in ?pn2 [:sim :queues (:target arc)]
                               #(into % (subvec (-> ?pn2 :sim :pulled) 0 mult)))
                    ;; Trim what can be assigned.
                    (update-in ?pn2 [:sim :pulled] #(subvec % mult)))))
              ?pn
              arcs-out)
      ;; Move what is left in :pulled to :removed. (pull-tokens will zero-out :pulled.)
      (assoc-in ?pn [:sim :removed] (-> ?pn :sim :pulled)))))

(defn flow-balance
  "Compute the difference: tokens out minus tokens in."
  [pn trans]
  (- (reduce (fn [sum arc] (+ sum (:multiplicity arc)))
             0
             (pnu/arcs-outof pn trans))
     (reduce (fn [sum arc] (+ sum (:multiplicity arc)))
             0
             (remove #(= (:type %) :inhibitor) (pnu/arcs-into pn trans)))))

(defn step-state
  "Update the (-> pn :sim :queues) for the effect of firing the argument transition."
  [pn link]
  (let [fire (:fire link)
        mkey (:marking-key pn)
        a-in (remove #(= :inhibitor (:type %)) (pnu/arcs-into pn fire))
        a-out (pnu/arcs-outof pn fire)
        balance (flow-balance pn fire)]
    (pnu/as-pn-ok-> pn ?pn
      (assoc-in ?pn [:sim :old-queues] (-> ?pn :sim :queues))
      ;; Pull from queues. Set (-> :pn :sim :pulled) to a list of tokens on the move.
      (pull-tokens ?pn a-in)   
      ;; Add (to the front) whatever more we'll need to satisfy out-going arcs. 
      (update-in ?pn [:sim :pulled] #(if (> balance 0) (into (new-tokens ?pn balance) %) %))
      ;; Move tokens from :pulled to the target queues. Place rest (if any) in :removed. 
      (push-tokens ?pn a-out)
      ;; Note change in queues with log messages.
      (update-log-for-step ?pn fire) 
      #_(do (println "Queues:" (-> ?pn :sim :queues)) ?pn))))

(defn validate-pulled
  "Check that pulled doesn't have multiple of a token."
  ([pn] (validate-pulled pn "no message"))
  ([pn msg]
   (let [tkns (map :id (-> pn :sim :pulled))]
     (when (not= (count tkns) (count (distinct tkns)))
       (reset! diag pn)
       (throw (ex-info (str "Same token in pulled twice: " msg)
                       {:pulled (-> pn :sim :pulled)}))))
   pn))

(defn validate-queues
  "Check that queues do not have duplicate tokens."
  [pn]
  (let [tkns (map :id (-> pn :sim :queues vals flatten))]
    (when (not= (count tkns) (count (distinct tkns)))
      (reset! diag pn)
      (throw (ex-info "Same token found in two places."
                      {:queues (-> pn :sim :queues)}))))
  pn)

(defn validate-remove
  "There are two methods that remove could be calculated; check that
   they produce the same answer."
  [pn]
  (let [old-queues (-> pn :sim :old-queues)
        queues (-> pn :sim :queues)
        old (-> old-queues vals flatten set)
        new (-> queues     vals flatten set)
        removed1 (clojure.set/difference old new)
        removed2 (-> pn :sim :removed set)]
    (when (not= removed1 removed2)   ; POD This is probably temporary. 
      (reset! diag pn)
      (throw (ex-info "Calculations of removed differ."
                      {:rem1 removed1 :rem2 removed2}))))
  pn)

(defn validate-move
  "Throw errors when things go wrong with queues."
  [pn]
  (-> pn
      validate-pulled
      validate-queues
      validate-remove))

(defn moved-tkns
  "Study queues to determine what tokens have moved."
  [pn]
    (let [qs  (-> pn :sim :queues)
          oqs (-> pn :sim :old-queues)
          old (-> oqs vals flatten set)
          new (->  qs vals flatten set)
          remain  (clojure.set/intersection old new)
          find-at (fn [tkn queues] (some (fn [[key val]] (when (some #(= % tkn) val) key)) queues))]
      (reduce (fn [mvd stay]
                (if (= (find-at stay qs) (find-at stay oqs))
                  mvd
                  (conj mvd stay)))
              [] remain)))

(defn log-act
  "Create an entry in the log for an :act."
  [pn fire]
  (let [old (-> pn :sim :old-queues vals flatten set)
        new (-> pn :sim :queues     vals flatten set)
        added   (clojure.set/difference new old)
        moved (moved-tkns pn)]
    (if (contains? (pnu/name2obj pn fire) :rep) ; This is adding the :act. 
      (update-in pn [:sim :log] #(conj % (assoc (:rep (pnu/name2obj pn fire))
                                                :j
                                                (vec (map :id (clojure.set/union added moved)))
                                                :fire
                                                fire)))
      pn)))

(defn log-remove
  "Create an entry in the log for :motion :remove"
  [pn fire]
  (let [old (-> pn :sim :old-queues vals flatten set)
        new (-> pn :sim :queues     vals flatten set)
        removed (clojure.set/difference old new)]
    (reduce (fn [pn rem]
              (update-in pn [:sim :log] #(conj % {:on-act fire :tkn rem :motion :remove})))
            pn removed)))

(defn log-add
  "Create an entry in the log for :motion :add"
  [pn fire]
  (let [old (-> pn :sim :old-queues vals flatten set)
        new (-> pn :sim :queues     vals flatten set)
        added    (clojure.set/difference new old)]
    (reduce (fn [pn add]
              (update-in pn [:sim :log] #(conj % {:on-act fire :tkn add :motion :add})))
            pn added)))

(defn log-move
  "Create an entry in the log for :motion :move"
  [pn fire]
  (let [qs  (-> pn :sim :queues)
        oqs (-> pn :sim :old-queues)
        old (-> oqs vals flatten set)
        new (->  qs vals flatten set)
        find-at (fn [tkn queues] (some (fn [[key val]] (when (some #(= % tkn) val) key)) queues))
        moved (moved-tkns pn)]
    (reduce (fn [pn mv]
              (update-in pn [:sim :log] #(conj % {:on-act fire :tkn mv :motion :move
                                                  :from (find-at mv oqs)
                                                  :to   (find-at mv qs)})))
            pn moved)))
  
(defn update-log-for-step
  "Add to log queue :add :remove and :move actions and :act from firing a transition."
  [pn fire]
  (-> pn
      (validate-move)
      (log-act    fire)
      (log-remove fire)
      (log-add    fire)
      (log-move   fire)
      (assoc-in [:sim :max-tkn] (max-tkn pn))))

(defn pick-link
  "Return a random link according to the distribution provide by their rates."
  [links]
  (let [r (rand (reduce (fn [sum l] (+ sum (:rate l))) 0.0 links))]
    (loop [dist links
           sum (:rate (first links))]
      (if (> sum r)
        (first dist)
        (recur (rest dist)
               (+ sum (:rate (second dist))))))))

;;; :queues {:place-1 [], :place-2 [], :place-3 [{:jtype :blue, :id 1}], :place-4 []},
(defn max-tkn
  "Return the max-tkn found in the marking."
  [pn]
  (reduce (fn [mx queue]
            (let [ids (map :id queue)]
              (if (empty? ids)
                mx
                (max mx (apply max ids)))))
          0
          (-> pn :sim :queues vals)))
