(ns pdenno.pn-draw.core
  "Petri net draw code"
  (:require [clojure.spec.alpha :as s]
            [clojure.math.combinatorics :as combo]
            [quil.core :as q]
            [quil.middleware :as qm]
            [pdenno.pn-draw.util :as pndu :refer (ppprint ppp)]
            [pdenno.spntools.utils :as pnu]
            [pdenno.pn-draw.simulate :as sim]))

;;; ToDo: * Replace pn-trans-point: review everything on the trans and distribute
;;;         so that things are on the correct side, not overlapping, and spaced nicely.
;;;         Step 1: define crossed? DONE.
;;;       * Show :intro :elim acts (and their multiplicity).
;;;       * Integrate simulation stepping capability.  
;;;       - Prevent coincident lines. (Example in m2-j2-bas.xml) 
;;;       - Find a way that labels can get focus despite their proximity to objects.
;;;       - selective redraw

(declare pn-geom basic-candidates trans-connects draw-tkn)
(declare nearest-elem ref-points draw-elem draw-arc! draw-tokens swap-crossed!)
(declare arc-coords arrowhead-coords pt-from-head handle-sim-step!)
(declare angle crossed? hilite-elem! handle-move-or-button! handle-move! rotate-trans!)
(declare arc-place-geom arc-trans-geom interesect-circle pn-trans-point trans-connects)
(declare calc-new-geom match-geom best-match messed-up-taken? eliminate-taken-dups)

(def params (let [data {:window-length 1100,   
                        :window-height 500,
                        :x-start 30,                       ; Where to start PN drawing
                        :y-start 30                        ; Where to start PN drawing
                        :button-close 30                   ; Region were buttons are active
                        :place-dia 26
                        :trans-width 36
                        :trans-height 8
                        :trans-prefer-center? true
                        :token-dia 4
                        :inhibit-dia 10
                        :arrowhead-length 12
                        :arrowhead-angle (/ Math/PI 8.0) ; zero is on the shaft.
                        :image-files {:right-arrow "resources/public/images/small-blue-arrow.jpg"}}]
              (as-> data ?d
                (assoc ?d :right-arrow                     ; Placement of right-arrow.
                       {:x-pos (- (:window-length ?d) 60)
                        :y-pos 10 :size 50}))))
                                        
(def lock-mouse-on (atom nil)) ; clojurescript: no alter-var-root, no with-local-vars
(def hilite-elem (atom nil))
(def the-pn (atom nil))

#?(:clj  (defn now [] (System/currentTimeMillis)))
#?(:cljs (defn now [] (.getTime (js/Date.))))

(defn rotate [x y theta]
  "Rotate (x,y) theta radians about origin."
  {:x (double (- (* (Math/cos theta) x) (* (Math/sin theta) y)))
   :y (double (+ (* (Math/sin theta) x) (* (Math/cos theta) y)))}) 

;;;   8----7----6----2----3----4----5
;;;   |                             |
;;;   1           (xc,yc)           0
;;;   |                             |
;;;   15---14---13---9---10---11---12
(def +rot-offsets+
  "Vector of vectors, one for each rotation. Each 16-place sub-vector is a set of 
   offsets from the center of the transition (xc,yc) with index as shown."
  (let [tw (/ (:trans-width params)  2.0)
        th (/ (:trans-height params) 2.0)
        p3 (* 0.3333333 tw)
        p4 (* 0.6666666 tw)
        p5 (* 0.8555555 tw) ; 0.950 in draw.cljs Not quite as drawn above. This looks better. 
        srotate (fn [x y theta] (vec (map #(Math/round %) (vals (rotate x y theta)))))
        ;; These are in window coordinates (0, 0 is upper left)
        base [[tw 0.0] [(- tw) 0.0]
              [0.0 (- th)] [p3 (- th)] [p4 (- th)] [p5 (- th)]
              [(- p3)(- th)] [(- p4) (- th)] [(- p5) (- th)]
              [0.0 th] [p3 th] [p4 th] [p5 th]
              [(- p3) th] [(- p4) th] [(- p5) th]]]
    (map #(let [theta (* Math/PI 0.25 %)]
            (map (fn [[x y]] (srotate x y theta)) base))
         (range 8))))

(def right-arrow (atom nil))

(defn setup-pn []
  (reset! right-arrow (q/load-image (-> params :image-files :right-arrow)))
  (q/frame-rate 20)    ; FPS. 10 is good
  (q/text-font (q/create-font "DejaVu Sans" 14 true)) ; pre-JMS was 12. 
  (q/background 200)) ; light grey

(defn draw-pn []
  (when-let [pn @the-pn]
    (q/background 230) ; POD not sure I want to keep this.
    (q/stroke 0) ; black
    (q/fill 255) ; white
    (q/stroke-weight 1)
    (q/image @right-arrow 
             (-> params :right-arrow :x-pos)
             (-> params :right-arrow :y-pos))
    (hilite-elem! pn)
    (if (q/mouse-pressed?)
      (handle-move-or-button!)
      (reset! lock-mouse-on nil))
    (doseq [place (:places pn)]
      (draw-elem pn place))
    (doseq [trans (:transitions pn)]
      (draw-elem pn trans))
    (doseq [arc (:arcs pn)]
      (draw-arc! pn arc))
    (swap-crossed!)
    (when (messed-up-taken? @the-pn)
      (swap! the-pn eliminate-taken-dups))))
     
(def ^:private diag (atom nil))

(defn on-button?
  [button-name]
  (let [binfo (button-name params)
        x (:x-pos binfo)
        y (:y-pos binfo)
        size (/ (:size binfo) 2)
        mx (q/mouse-x)
        my (q/mouse-y)]
    (and (< (- x size) mx (+ x size))
         (< (- y size) my (+ y size)))))

(defn handle-move-or-button! []
  (if (on-button? :right-arrow)
    (handle-sim-step!)
    (handle-move!)))

(defn update-marking
  [pn]
  (reduce (fn [pn [place cnt]]
            (let [ix (pnu/place-index pn place)]
              (assoc-in pn [:places ix :initial-tokens] cnt)))
          pn
          (zipmap
           (:marking-key pn)
           (map count (sim/queues-marking-order pn)))))

(def last-step (atom 0))

(defn handle-sim-step!
  "Update the-pn marking to reflect one (stochastic) step."
  []
  (when (> (- (now) @last-step) 500) ; avoid stuttering.
     (reset! last-step (now))
     (swap! the-pn
            #(-> %
                 sim/simulate
                 update-marking))))

(defn handle-move!
  "Mouse pressed: Update coordinates to move an element or its label."
  []
  (when-let [elem (or @lock-mouse-on (nearest-elem @the-pn [(q/mouse-x) (q/mouse-y)]))]
    (swap! the-pn
           #(let [n (:name elem)]
              (if (:label? elem)
                (as-> % ?pn
                  (assoc-in ?pn [:geom n :label-x-off]
                            (- (q/mouse-x) (-> ?pn :geom n :x)))
                  (assoc-in ?pn [:geom n :label-y-off]
                            (- (q/mouse-y) (-> ?pn :geom n :y))))
                (as-> % ?pn
                  (assoc-in ?pn [:geom n :x] (q/mouse-x))
                  (assoc-in ?pn [:geom n :y] (q/mouse-y))))))))

(defn hilite-elem!
  "Set hilite-elem and maybe lock-mouse-on."
  [pn]
  (let [nearest (or @lock-mouse-on (nearest-elem pn [(q/mouse-x) (q/mouse-y)]))]
    (when (and nearest (q/mouse-pressed?))
      (reset! lock-mouse-on nearest))
    (if nearest
      (reset! hilite-elem nearest)
      (reset! hilite-elem nil))))

(defn nearest-elem
  "Return a element (place/trans) map indicating what was closest to the mouse.
  :label? in the map indicates it was the elem's label that was closest.
   Returns nil if nothing is close."
  [pn mxy]
  (let [[bkey min-d label?]
        (reduce
         (fn [[bkey min-d label?] [key val]]
           (let [delem  (Math/round (pndu/distance (into mxy (vector (:x val) (:y val)))))
                 dlabel (Math/round (pndu/distance (into mxy (vector (+ (:x val) (:label-x-off val))
                                                                (+ (:y val) (:label-y-off val))))))
                 min? (min delem dlabel min-d)]
             (if (= min? delem)
               [key min? false]
               (if (= min? dlabel)
                 [key min? true]
                 [bkey min-d label?]))))
         [:not-set 99999 true]
         (-> pn :geom))]
    (when (< min-d 30.0)
      (when-let [elem (or (some #(when (= bkey (:name %)) %) (:places pn))
                          (some #(when (= bkey (:name %)) %) (:transitions pn)))]
        (assoc elem :label? label?)))))

(def half-tw (Math/round (/ (:trans-width params) 2.0)))
(def half-th (Math/round (/ (:trans-height params) 2.0)))
(def rots (map #(* Math/PI 0.25 %) (range 8)))
(defn draw-trans
  "Draw a transition with rotation as indicated by :rotate."
  [pn name x y]
  (q/with-translation [x y] ;[(+ x half-tw) (+ y half-th)]
    (q/with-rotation [(if-let [n (-> pn :geom name :rotate)]
                        (nth rots n)
                        0.0)]
      (q/rect (- half-tw) (- half-th) (:trans-width params) (:trans-height params)))))

(defn draw-elem
  "Draws a element and its label. The element map might have :label? = true
   to indicate that it is the label that needs highlighting. The actual 
   updating of the net is taken care of in handle-mouse."
  [pn elem]
  (let [n (:name elem)
        x (-> pn :geom n :x)
        y (-> pn :geom n :y)
        hilite @hilite-elem]
    (q/fill (if (and (= n (:name hilite))
                     (:label? hilite))
              (q/color 255 0 0) 0))
    (q/text (name n)
            (+ x (-> pn :geom n :label-x-off))
            (+ y (-> pn :geom n :label-y-off)))
    (q/fill 0)
    (q/stroke (if (and (= (:name hilite) n) (not (:label? hilite))) (q/color 255 0 0) 0))
    (if (pndu/place? elem)
      (do
        (q/fill 255)
        (q/ellipse x y (:place-dia params) (:place-dia params))
        (draw-tokens (:initial-tokens elem) x y))
      ;; It's a transition
      (do (q/fill (if (= (:type elem) :immediate) 0 255))
          (draw-trans pn n x y)))
    (q/stroke 0)
    (q/fill 0)))

(defn draw-tokens
  [cnt x y]
  (let [d (+ (:token-dia params) 1)]
    (case cnt
      0 :do-nothing
      1 (draw-tkn x y)
      2 (do (draw-tkn (- x d) (- y d))
            (draw-tkn (+ x d) (+ y d)))
      3 (do (draw-tkn (- x d) (- y d))
            (draw-tkn (+ x d) (+ y d))
            (draw-tkn x y))
      4 (do (draw-tkn (- x d) (- y d))
            (draw-tkn (+ x d) (+ y d))
            (draw-tkn (- x d) (+ y d))
            (draw-tkn (+ x d) (- y d)))
      5 (do (draw-tkn (- x d) (- y d))
            (draw-tkn (+ x d) (+ y d))
            (draw-tkn (- x d) (+ y d))
            (draw-tkn (+ x d) (- y d))
            (draw-tkn x y))
      (q/text (str cnt) x y))))

(defn draw-tkn
  [x y]
  (let [dia (:token-dia params)]
    (q/fill 0) ; (q/fill 0 0 255) ; blue
    (q/ellipse x y dia dia)))

(defn multiplicity-pos
  "Coordinates of arc multiplicity text (number)."
  [{x1 :tx y1 :ty x2 :px y2 :py}]
  (let [offset 14
        offset-angle 0.7854 ; pi/4
        xmid   (/ (+ x1 x2) 2.0)
        ymid   (/ (+ y1 y2) 2.0)
        {xnear :x ynear :y} (pt-from-head x1 y1 xmid ymid offset)
        len (pndu/distance xmid ymid xnear ynear)
        angle (pndu/angle x1 y1 x2 y2)
        xl (+ len (* offset (Math/cos (- Math/PI offset-angle))))
        yl (* offset (Math/sin (- Math/PI offset-angle)))
        lrotate (rotate xl yl angle)]
    {:x (Math/round (+ xmid (:x lrotate)))
     :y (Math/round (+ ymid (:y lrotate)))}))

(defn draw-arc!
  [pn arc]
  (let [place-is-head? (pndu/place? (pndu/name2obj pn (:target arc))),
        inhibitor? (= :inhibitor (:type arc)),
        ln (if place-is-head? 
             (arc-coords pn (:source arc) (:target arc) (:name arc))
             (arc-coords pn (:target arc) (:source arc) (:name arc))),
        trans (:trans ln),
        take (:take ln)]
    (swap! the-pn (fn [pn]
                    (-> pn
                        (update-in [:geom trans :taken] #(assoc % (:name arc) take))
                        (assoc-in  [:geom-arcs (:name arc)] {:px (:px ln) :py (:py ln) 
                                                             :tx (:tx ln) :ty (:ty ln)}))))
    (when-not (== 1 (:multiplicity arc))
      (let [midt (multiplicity-pos ln)]
        (q/fill 0)
        (q/text (str (:multiplicity arc)) (:x midt) (:y midt))))
    (if inhibitor? ; head is always the transition
      (let [inhibit-dia (:inhibit-dia params)
            center (pt-from-head (:px ln) (:py ln) (:tx ln) (:ty ln) (- (/ inhibit-dia 2)))
            end (pt-from-head (:px ln) (:py ln) (:tx ln) (:ty ln) (- inhibit-dia))]
        (q/fill 255)
        (q/stroke-weight 1)
        (q/ellipse (:x center) (:y center) inhibit-dia inhibit-dia)
        (q/stroke-weight 1)
        (q/line (:px ln) (:py ln) (:x end) (:y end)))
      (do (cond (= (:draw-color arc) :red)  (do (q/stroke 255 0 0) (q/stroke-weight 2))
                (= (:draw-color arc) :blue) (do (q/stroke 0 0 255) (q/stroke-weight 2))
                :else (do (q/stroke 0 0 0) (q/stroke-weight 1)))
          (q/line (:tx ln) (:ty ln) (:px ln) (:py ln))
          (if place-is-head?
            (let [ahc (arrowhead-coords (:tx ln) (:ty ln) (:px ln) (:py ln))]
              (q/line (:px ln) (:py ln) (:xl ahc) (:yl ahc))
              (q/line (:px ln) (:py ln) (:xr ahc) (:yr ahc)))
            (let [ahc (arrowhead-coords (:px ln) (:py ln) (:tx ln) (:ty ln))]
              (q/line (:tx ln) (:ty ln) (:xl ahc) (:yl ahc))
              (q/line (:tx ln) (:ty ln) (:xr ahc) (:yr ahc))))))))

(defn arrowhead-coords
  "Provide coordinates for the two points at the end of the edges of arrow at (x2,y2)"
  [x1 y1 x2 y2]
  (let [alen (:arrowhead-length params)
        aangle (:arrowhead-angle params)
        len (pndu/distance x1 y1 x2 y2)
        angle (pndu/angle x1 y1 x2 y2) 
        xl (+ len (* alen (Math/cos (- Math/PI aangle))))
        xr (+ len (* alen (Math/cos (+ Math/PI aangle))))
        yl (* alen (Math/sin (- Math/PI aangle)))
        yr (* alen (Math/sin (+ Math/PI aangle)))
        lrotate (rotate xl yl angle)
        rrotate (rotate xr yr angle)]
    {:xl (+ x1 (:x lrotate)) :yl (+ y1 (:y lrotate))
     :xr (+ x1 (:x rrotate)) :yr (+ y1 (:y rrotate))}))
  
(defn pt-from-head
  "Return a point d units beyond (or within, if negative) the line segment."
  [x1 y1 x2 y2 d]
  (let [len (pndu/distance x1 y1 x2 y2)
        ratio (/ (+ len d) len)]
    {:x (+ x1 (* ratio (- x2 x1)))
     :y (+ y1 (* ratio (- y2 y1)))}))

(defn arc-coords
  "Return arc coordinates for argument arc (has aid)."
  [pn trans place arc]
  (merge (arc-trans-geom pn trans place arc)
         (arc-place-geom pn trans place arc)))

;;;   8----7----6----2----3----4----5
;;;   |                             |
;;;   1           (xc,yc)           0
;;;   |                             |
;;;   15---14---13---9---10---11---12
(defn arc-trans-geom
  "Return {:tx x :ty y :take <n>} of the best place for the argument arc to 
   connect to the trans. Considers rotation and other occupancy on the trans."
  [pn trans place arc]
  (let [me-now (-> pn :geom trans :taken arc)
        ;; POD Current bug(!) means duplicates in taken. I'm careful not to remove a duplicate.
        taken (-> pn :geom trans :taken (dissoc arc) vals set)
        t-connects (trans-connects pn trans) ; vector of 16 connection points.
        [cx cy tx ty] (ref-points pn place trans) ; these are center points
        D (zipmap (range 16) (map (fn [txy] (pndu/distance (into [cx cy] txy))) t-connects))
        top-showing?  (< (get D 2) (get D 9))
        left-showing? (< (get D 1) (get D 0))
        candidates (->> (basic-candidates top-showing? left-showing?) ; subset of (range 16)
                        (remove #(taken %)))
        y-diff (Math/abs (- cy ty)) ; y difference between centers.
        best (cond
               (and (< y-diff 5) left-showing? (not (taken 1))) 1 ; horizontally aligned
               (and (< y-diff 5) (not (taken 0)))               0 ; horizontally aligned
               (and (:trans-prefer-center? params) top-showing?       (not (taken 2))) 2
               (and (:trans-prefer-center? params) (not top-showing?) (not (taken 9))) 9
               :else (->> candidates
                          (map #(vector % (get D %)))
                          (sort (fn [[_ d1] [_ d2]] (< d1 d2)))
                          first
                          first))
        coords (nth t-connects best)]
    {:tx (first coords) :ty (second coords) :take best}))

(defn dedup-val
  "Remove arbitrarily one of the key/value pairs that has a duplicate value or nil value."
  [m]
  (reduce-kv (fn [accum k v]
               (if (or (not v) (some #(= (second %) v)  accum))
                 accum
                 (assoc accum k v)))
             {}
             m))

;;; POD Should not be necessary!
(defn messed-up-taken?
  "Check whether there are duplicate :taken values anywhere."
  [pn]
  (some #(when-let [takes (not-empty (-> % :taken vals))]
           (when (or (some (fn [v] (not v)) takes)
                     (not (apply distinct? takes))) %))
        (-> pn :geom vals)))

(defn eliminate-taken-dups
  "For reasons not discovered, duplicates in arc :taken arise.
   This removes them."
  [pn]
  (update pn :geom
          #(reduce-kv (fn [accum k v]
                        (if (contains? v :taken)
                          (assoc accum k (update v :taken dedup-val))
                          (assoc accum k v)))
                      {}
                      %)))

(defn trans-connects
  "Return a vector of [x, y] being the 16 connection points on a transition"
  [pn trans]
  (let [[rx ry] (ref-points pn trans)
        rotation (or (-> pn :geom trans :rotate) 0)
        offsets (nth +rot-offsets+ rotation)]
    (vec (map (fn [[xoff yoff]] (vector (+ rx xoff) (+ ry yoff))) offsets))))

(defn basic-candidates
  "Return candidate connection positions given knowledge of what's showing."
  [top-showing? left-showing?]
  (cond (and top-showing? left-showing?)
        [8 7 6 2 3 4 5 1]
        top-showing?
        [8 7 6 2 3 4 5 0]
        left-showing?
        [15 14 13 9 10 11 12 1]
        :else
        [15 14 13 9 10 11 12 0]))
  
(defn ref-points
  "Return a vector of [x, y,...] for each named object."
  [pn & names]
  (reduce (fn [v name]
            (-> v
                (conj (-> pn :geom name :x))
                (conj (-> pn :geom name :y))))
          []
          names))

(defn arc-place-geom
  "Produce a map {:px x :py y} for the position where arc meets place."
  [pn trans place arc]
  (let [[tx ty px py] (ref-points pn trans place) ; both are center points
        bc (pndu/intersect-circle ; base
            (double (- tx px))
            (double (- ty py))
            0.0
            0.0
            (/ (:place-dia params) 2.0))
        tc {:x1 (+ (:x1 bc) px) ; translated
            :y1 (+ (:y1 bc) py)
            :x2 (+ (:x2 bc) px)
            :y2 (+ (:y2 bc) py)}]
    (merge {:trans trans :place place}
           ;; choose closest intersection on circle
           (if (< (pndu/distance (:x1 tc) (:y1 tc) tx ty)
                  (pndu/distance (:x2 tc) (:y2 tc) tx ty))
             {:px (-> tc :x1 Math/round) :py (-> tc :y1 Math/round)}
             {:px (-> tc :x2 Math/round) :py (-> tc :y2 Math/round)}))))

(def last-rot (atom 0))

(defn pn-wheel-fn!
  [_]
  (when-let [hilite @hilite-elem]
    (when (contains? hilite :tid)
      (let [time-now (now)]
        (when (> (- time-now @last-rot) 350)
          (reset! last-rot time-now)
          (swap! the-pn
                 (fn [pn] (update-in pn
                                     [:geom (:name hilite)]
                                     #(assoc % :rotate
                                             (if-let [rot (:rotate %)]
                                               (mod (inc rot) 8)
                                               1))))))))))

;;;---------------- window/rescaling  stuff ------------------------
(defn pn-graph-scale
  "Return a map providing reasonable scale factor for displaying the graph,
   given that the PN might have originated with another tool."
  [geom]
  (let [range
        (reduce (fn [range xy]
                  (as-> range ?r
                    (assoc ?r :min-x (min (:min-x ?r) (:x xy)))
                    (assoc ?r :max-x (max (:max-x ?r) (:x xy)))
                    (assoc ?r :min-y (min (:min-y ?r) (:y xy)))
                    (assoc ?r :max-y (max (:max-y ?r) (:y xy)))))
                {:min-x 99999 :max-x -99999
                 :min-y 99999 :max-y -99999}
                (vals geom))
        length (- (:max-x range) (:min-x range))
        height (- (:max-y range) (:min-y range))]
    (as-> {} ?r
      (assoc ?r :scale (* 0.8 (min (/ (:window-length params) length)
                                   (/ (:window-height params) height))))
      (assoc ?r :x-off (- (:x-start params) (:min-x range)))
      (assoc ?r :y-off (- (:y-start params) (:min-y range))))))

(defn pn-geom
  "Compute reasonable display placement (:geom) for the argument PN."
  ([pn]
   (if (contains? pn :geom)
     pn
     (assoc pn :geom (calc-new-geom pn))))
  ([pn old-pn]
   (if (contains? old-pn :geom)
     (assoc pn :geom (match-geom pn old-pn))
     (assoc pn :geom (calc-new-geom pn)))))

(defn match-geom
  "Set the geom to match positions of stuff on the screen now, as much as possible."
  [pn old-pn]
  (let [new-geom (calc-new-geom pn)]
    (let [old-geom (atom (:geom old-pn))]
      (reduce (fn [geom [key val]]
                (if-let [old-val (key @old-geom)]
                  (do (swap! old-geom #(dissoc % key))
                      (assoc geom key old-val))
                  (let [[kill-key new-val] (best-match pn old-pn new-geom @old-geom key)]
                    (swap! old-geom #(dissoc % kill-key)) ; kill-key may be nil (no good match, new-val from new-geom). 
                    (assoc geom key new-val))))
              {}
              new-geom))))

(defn best-match
  "Return the [key, object] from old-geom that best matches the argument obj."
  [pn old-pn new-geom old-geom key]
  ;; Find the other (not key) side of arcs into and out of the key object.
  ;; If these arcs also name things that are in the old-pn, return the geometry
  ;; This is NOT foolproof! Return the old-pn.geom and new or old geometry object. 
  (let [new-arcs (:arcs pn)                 
        into-new  (some #(when (= key (:target %)) (:source %)) new-arcs)
        outof-new (some #(when (= key (:source %)) (:target %)) new-arcs)
        old-arcs (:arcs old-pn)                 
        arc-in  (some #(when (= into-new  (:target %)) %) old-arcs)
        arc-out (some #(when (= outof-new (:source %)) %) old-arcs)]
    (if (and arc-in arc-out (= (:target arc-in) (:source arc-out)))
      [(:target arc-in) ((:target arc-in) old-geom)]
      [nil (key new-geom)])))

(defn rescale
  "Modifiy :geom to fit window-params"
  [geom params]
  (let [scale (:scale params)
        xs (:x-off params)
        ys (:y-off params)]
    (reduce (fn [mp [key val]]
              (assoc mp key
                     (-> val
                         (assoc :x (Math/round (* scale (+ xs (-> val :x)))))
                         (assoc :y (Math/round (* scale (+ ys (-> val :y)))))
                         (assoc :label-x-off (max 10 (Math/round (* 0.6 scale (-> val :label-x-off)))))
                         (assoc :label-y-off (max 10 (Math/round (* 0.6 scale (-> val :label-y-off))))))))
            {}
            geom)))

(defn calc-new-geom
  "No geometry exists for this pn; spread it out in a circle."
  [pn]
  (let [places (->> pn :places (map :name))
        trans  (->> pn :transitions (map :name))
        elems  (-> (vec (interleave trans places))
                   (into trans)
                   (into places)
                   (distinct))
        angle-inc (/ (* 2 Math/PI) (count elems))]
    (let [angle (atom (- angle-inc))]
      (let [geom (reduce (fn [geom ename]
                           (swap! angle #(+ % angle-inc))
                           (assoc geom ename
                                  {:x (Math/round (* 100 (Math/cos @angle)))
                                   :y (Math/round (* 100 (Math/sin @angle)))
                                   :label-x-off 10
                                   :label-y-off 15}))
                         {}
                         elems)]
        (rescale geom (pn-graph-scale geom))))))

(defn eqn
  "Return a vector [a,b,c] for line eqn  ax + by = c given two points."
  [x1 y1 x2 y2]
  (when-not (== x1 x2)
    (let [[x1 y1 x2 y2] (mapv double [x1 y1 x2 y2])
          slope   (/ (- y2 y1) (- x2 x1))
          y-cept  (- y1 (* slope x1))
          a (- slope)
          b 1 
          c y-cept]
      [a b c])))

(defn crossed?
  "Return true if the line segments defined by the points cross inside their length."
  [x1 y1 x2 y2 x3 y3 x4 y4]
  (let [[a b b1] (eqn x1 y1 x2 y2)
        [c d b2] (eqn x3 y3 x4 y4)
        det (and a c (- (* a d) (* b c)))]
    (if (or (not det)
            (< (Math/abs det) 0.00000001))
      false
      (let [aa (/ d det)
            bb (- (/ b det))
            cc (- (/ c det))
            dd (/ a det)
            x (+ (* aa b1) (* bb b2))
            y (+ (* cc b1) (* dd b2))]
        (and (< 0.0 (/ (- x x1) (- x2 x1)) 1.0)
             (< 0.0 (/ (- x x3) (- x4 x3)) 1.0)
             (< 0.0 (/ (- y y1) (- y2 y1)) 1.0)
             (< 0.0 (/ (- y y3) (- y4 y3)) 1.0))))))

(declare share-trans)
(defn swap-crossed!
  "Check the-pn for crossed arcs onto transitions.
   Swap them if they are crossed."
  []
  (let [pn @the-pn
        swaps (reduce-kv (fn [accum k v]
                           (let [result (filter (fn [[a1 a2]]
                                                  (let [arc1 (-> pn :geom-arcs a1)
                                                        arc2 (-> pn :geom-arcs a2)]
                                                    (when (and arc1 arc2)
                                                      (crossed? (:tx arc1) (:ty arc1) (:px arc1) (:py arc1)
                                                                (:tx arc2) (:ty arc2) (:px arc2) (:py arc2)))))
                                                v)]
                             (if (not-empty result)
                               (assoc accum k result)
                               accum)))
                         {}
                         (share-trans pn))]
    (when (not-empty swaps)
      (doseq [[trans [[a1 a2]]] swaps]
        (let [geom (-> pn :geom trans)
              save1 (a1 geom)
              save2 (a2 geom)]
          (swap! the-pn
                 #(-> %
                      (assoc-in [:geom trans :taken a1] save2)
                      (assoc-in [:geom trans :taken a2] save1))))))))

(defn share-trans [pn]
  "Return a map of vectors of arc pairs that share a connection to a transition."
  (reduce (fn [accum trans]
            (assoc accum
                   trans
                   (map vec
                        (combo/combinations
                         (->> (filter #(or (= (:source %) trans)
                                           (= (:target %) trans))
                                      (:arcs pn))
                              (map :name))
                         2))))
          {}
          (->> pn :transitions (map :name))))
  
;;;==== spec-based validation =========================
(defn geom-ok? 
  "If the pn has :geom, it must have an entry for every place and transition."
  [pn]
  (let [names   (-> (map :name (:places pn))
                    (into (map :name (:transitions pn))))
        geom (:geom pn)]
    (if geom
      (every? #(contains? geom %) names)
      true)))

(defn connect-ok? 
  "Check that the :source and :target of every arc is defined."
  [pn]
  (let [tnames (map :name (:transitions pn))
        pnames (map :name (:places pn))]
    (or (every? (fn [arc]
                 (or 
                  (and (some #(= (:source arc) %) tnames)
                       (some #(= (:target arc) %) pnames))
                  (and (some #(= (:target arc) %) tnames)
                       (some #(= (:source arc) %) pnames))))
                (:arcs pn)))))

(s/def ::type (fn [t] (some #(= t %) [:normal :inhibitor :exponential :immediate])))
(s/def ::target keyword?)
(s/def ::source keyword?)
(s/def ::multiplicity (s/and integer? pos?))
(s/def ::aid (s/and integer? #(not (neg? %))))
(s/def ::tid (s/and integer? #(not (neg? %))))
(s/def ::pid (s/and integer? #(not (neg? %))))
(s/def ::name keyword?)
(s/def ::arc (s/keys :req-un [::aid ::source ::target ::name ::multiplicity]))
(s/def ::transition (s/keys :req-un [::name ::tid ::type ::rate]))
(s/def ::place (s/keys :req-un [::name ::pid ::initial-tokens]))
(s/def ::transitions (s/and (s/coll-of ::transition :kind vector? :min-count 1)
                            #(->> % (map :name) distinct?)))
(s/def ::arcs    (s/and (s/coll-of ::arc :kind vector? :min-count 1)
                        #(->> % (map :name) distinct?)))
(s/def ::places  (s/and (s/coll-of ::place :kind vector? :min-count 1)
                        #(->> % (map :name) distinct?)))
(s/def ::draw-pn (s/and (s/keys :req-un [::places ::arcs ::transitions])
                        #(geom-ok? %)
                        #(connect-ok? %)))
(s/def ::mark-val (s/int-in 0 10000)) ; cljs doesn't have ##Inf. 
(s/def ::marking (s/coll-of ::mark-val :kind vector?))

;;;============== REPL utils =============
(defn missing-connection
  "REPL utility (not used in spec), return arcs for which
   a source or target is missing." 
  [pn]
  (let [tnames (map :name (:transitions pn))
        pnames (map :name (:places pn))]
    (remove (fn [arc]
              (or 
               (and (some #(= (:source arc) %) tnames)
                    (some #(= (:target arc) %) pnames))
               (and (some #(= (:target arc) %) tnames)
                    (some #(= (:source arc) %) pnames))))
            (:arcs pn))))

(defn missing-geom
  "REPL utility (not used in spec).
   Returns a collection of elements that don't have :geom"
  [pn]
  (let [geom (:geom pn)]
    (remove #(contains? geom %)
            (-> (map :name (:places pn))
                (into (map :name (:transitions pn)))))))

(def jms-figs
  {:fig-5  "resources/public/PNs/jms/fig-5.clj"    ; 2-machine needs token
   :fig-7  "resources/public/PNs/jms/fig-7.clj"    ; sloppy causal (low priority)
   :fig-8  "resources/public/PNs/jms/fig-8.clj"    ; Eden, no :wc-3-1
   :fig-9a "resources/public/PNs/jms/fig-9a.clj"   ; New, add-machine-restart folding
   :fig-9b "resources/public/PNs/jms/fig-9b.clj"   ; New, add-machine-restart folding
   :fig-10 "resources/public/PNs/jms/fig-10.clj"   ; Not interpreted, small labels.
   :fig-11 "resources/public/PNs/jms/fig-11.clj"   ; Interpreted, small labels.
   :fig-12 "resources/public/PNs/jms/fig-12.clj"}) ; mixed-model 

(defn fig [fig-num]
  (let [pn (load-file (get jms-figs fig-num))]
    (reset! the-pn (pn-geom pn))
    (q/defsketch best-pn 
      :host "Tryme-PN"
      :title "A Petri Net"
      :features [:keep-on-top]
      ;; Smooth=2 is typical. Can't use pixel-density with js.
      :settings #(fn [] (q/smooth 2)) 
      :setup setup-pn
      :draw draw-pn
      :mouse-wheel pn-wheel-fn!
      :size [(:window-length params)
             (:window-height params)])))
