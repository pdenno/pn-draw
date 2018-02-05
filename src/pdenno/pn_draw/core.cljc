(ns pdenno.pn-draw.core
  "Petri net draw code"
  (:require [quil.core :as q]
            [quil.middleware :as qm]
            [pdenno.pn-draw.util :as pndu :refer (ppprint ppp)]
            [gov.nist.spntools.util.utils :as pnu]
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
(declare nearest-elem ref-points draw-elem draw-arc draw-tokens)
(declare arc-coords arrowhead-coords pt-from-head handle-sim-step!)
(declare angle crossed? hilite-elem! handle-move-or-button! handle-move! rotate-trans!)
(declare arc-place-geom arc-trans-geom interesect-circle pn-trans-point trans-connects)
(declare calc-new-geom match-geom best-match)

(def params (let [data {:window-length 900,   
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
                        :image-files {:right-arrow "data/images/small-blue-arrow.jpg"}}]
              (as-> data ?d
                (assoc ?d :right-arrow                     ; Placement of right-arrow.
                       {:x-pos (- (:window-length ?d) 60)
                        :y-pos 10 :size 50}))))
                                        
(def lock-mouse-on nil)
(def hilite-elem nil)
(def the-pn nil)

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
    [(vec (map (fn [[x y]] (srotate x y 0.0)) base))
     (vec (map (fn [[x y]] (srotate x y (* Math/PI 0.25))) base))
     (vec (map (fn [[x y]] (srotate x y (* Math/PI 0.50))) base))
     (vec (map (fn [[x y]] (srotate x y (* Math/PI 0.75))) base))]))

(def right-arrow nil)

(defn setup-pn []
  (alter-var-root #'right-arrow
                  (constantly (q/load-image (-> params :image-files :right-arrow))))
  (q/frame-rate 20)    ; FPS. 10 is good
  (q/text-font (q/create-font "DejaVu Sans" 12 true))
  (q/background 200)) ; light grey

(defn draw-pn []
  (when-let [pn the-pn]
    (q/background 230) ; POD not sure I want to keep this.
    (q/stroke 0) ; black
    (q/fill 255) ; white
    (q/stroke-weight 1)
    (q/image right-arrow 
             (-> params :right-arrow :x-pos)
             (-> params :right-arrow :y-pos))
    (hilite-elem! pn)
    (if (q/mouse-pressed?)
      (handle-move-or-button!)
      (alter-var-root #'lock-mouse-on (constantly nil)))
    (doseq [place (:places pn)]
      (draw-elem pn place))
    (doseq [trans (:transitions pn)]
      (draw-elem pn trans))
    (doseq [arc (:arcs pn)]
      (draw-arc pn arc))))

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

(def last-step 0)

(defn update-marking
  [pn]
  (reduce (fn [pn [place cnt]]
            (let [ix (pnu/place-index pn place)]
              (assoc-in pn [:places ix :initial-tokens] cnt)))
          pn
          (zipmap
           (:marking-key pn)
           (map count (sim/queues-marking-order pn)))))

(defn handle-sim-step!
  "Update the-pn marking to reflect one (stochastic) step."
  []
  (when (> (- (now) last-step) 500) ; avoid stuttering.
    (alter-var-root #'last-step (constantly (now)))
    (println "Step it!")
    (alter-var-root #'the-pn
                    #(-> %
                         sim/simulate
                         update-marking))))

(defn handle-move!
  "Mouse pressed: Update coordinates to move an element or its label."
  []
  (when-let [elem (or lock-mouse-on (nearest-elem the-pn [(q/mouse-x) (q/mouse-y)]))]
    (alter-var-root
     #'the-pn
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
  (let [nearest (or lock-mouse-on (nearest-elem pn [(q/mouse-x) (q/mouse-y)]))]
    (when (and nearest (q/mouse-pressed?))
      (alter-var-root #'lock-mouse-on (constantly nearest)))
    (if nearest
      (alter-var-root #'hilite-elem (constantly nearest))
      (alter-var-root #'hilite-elem (constantly nil)))))

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
(def rots [0.0 (* Math/PI 0.25) (* Math/PI 0.50) (* Math/PI 0.75)])
(defn draw-trans
  "Draw a transition with rotation as indicated by :rotate."
  [name x y]
  (q/with-translation [x y] ;[(+ x half-tw) (+ y half-th)]
    (q/with-rotation [(if-let [n (-> the-pn :geom name :rotate)]
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
        hilite hilite-elem]
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
          (draw-trans n x y)))
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

(defn draw-arc
  [pn arc]
  (let [place-is-head? (pndu/place? (pndu/name2obj pn (:target arc))),
        inhibitor? (= :inhibitor (:type arc)),
        ln (if place-is-head? 
             (arc-coords pn (:source arc) (:target arc) (:name arc))
             (arc-coords pn (:target arc) (:source arc) (:name arc)))]
    (when-not (== 1 (:multiplicity arc))
      (let [midt (multiplicity-pos ln)]
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
      (do (q/line (:tx ln) (:ty ln) (:px ln) (:py ln))
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
  (let [me-now? (-> pn :geom trans :taken arc)
        t-connects (trans-connects pn trans)
        t-result (fn [take] (let [c (nth t-connects take)] {:tx (first c) :ty (second c) :take take}))]
    (if (and me-now? (not lock-mouse-on)) ; did it already assigned and elements are not being moved.
      (t-result me-now?)
      (let [[cx cy tx ty] (ref-points pn place trans) ; these are center points
            D (zipmap (range 16) (map (fn [txy] (pndu/distance (into [cx cy] txy))) t-connects))
            gfn (fn [n] [n (get D n)])
            top-showing? (< (get D 2) (get D 9))
            left-showing? (< (get D 1) (get D 0))
            closest (first (sort (fn [[_ d1] [_ d2]] (< d1 d2)) D))
            taken-map (-> the-pn :geom trans :taken)
            taken (set (remove #(= % me-now?) (vals taken-map)))
            not-taken? (fn [n] (not (contains? taken n)))
            y-diff (Math/abs (double (- cy ty))) 
            ;; candidates =  ([0 94.25498543]  [8 111.00450929]...) -- distances
            candidates (->> (basic-candidates D top-showing? left-showing?)
                             (remove (fn [c] (some #(= (first c) %) taken)))
                             (sort (fn [[_ d1] [_ d2]] (< d1 d2))))
            best (cond (empty? candidates) closest
                       (and (< y-diff 5) left-showing? (not-taken? 1)) (gfn 1) 
                       (and (< y-diff 5) (not-taken? 0)) (gfn 0)
                       (and (:trans-prefer-center? params) top-showing? (not-taken? 2)) (gfn 2)
                       (and (:trans-prefer-center? params) (not top-showing?) (not-taken? 9)) (gfn 9)
                       :else (first candidates))]
        (t-result (first best))))))

(defn trans-connects
  "Return a vector of [x, y] being the 16 connection points on a transition"
  [pn trans]
  (let [[rx ry] (ref-points pn trans)
        rotation (or (-> the-pn :geom trans :rotate) 0)
        offsets (nth +rot-offsets+ rotation)]
    (vec (map (fn [[xoff yoff]] (vector (+ rx xoff) (+ ry yoff))) offsets))))

(defn basic-candidates
  "First set of considerations when pick candidate connection positions."
  [D top-showing? left-showing?]
  (let [gfn (fn [n] [n (get D n)])]
    (cond (and top-showing? left-showing?)
          (conj (map gfn '(8 7 6 2 3 4 5)) (gfn 1)),
          top-showing?
          (conj (map gfn '(8 7 6 2 3 4 5)) (gfn 0)),
          left-showing?
          (conj (map gfn '(15 14 13 9 10 11 12)) (gfn 1)),
          :else
          (conj (map gfn '(15 14 13 9 10 11 12)) (gfn 0)))))
  
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
    ;; choose closest intersection on circle
    (if (< (pndu/distance (:x1 tc) (:y1 tc) tx ty)
           (pndu/distance (:x2 tc) (:y2 tc) tx ty))
      {:px (:x1 tc) :py (:y1 tc)}
      {:px (:x2 tc) :py (:y2 tc)})))

(def last-rot 0)

(defn pn-wheel-fn
  [_]
  (when-let [hilite hilite-elem]
    (when (contains? hilite :tid)
      (let [time-now (now)]
        (when (> (- time-now last-rot) 250)
          (alter-var-root #'last-rot (constantly time-now))
          (alter-var-root #'the-pn
                          (fn [pn]
                            (update-in pn
                                       [:geom (:name hilite)]
                                       #(cond (not (contains? % :rotate)) (assoc % :rotate 1), 
                                              (= 1 (:rotate %)) (assoc % :rotate 2),
                                              (= 2 (:rotate %)) (assoc % :rotate 3),
                                              :else (dissoc % :rotate))))))))))

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
    (with-local-vars [old-geom (:geom old-pn)]
      (reduce (fn [geom [key val]]
                (if-let [old-val (key @old-geom)]
                  (do (var-set old-geom (dissoc @old-geom key))
                      (assoc geom key old-val))
                  (let [[kill-key new-val] (best-match pn old-pn new-geom @old-geom key)]
                    (var-set old-geom (dissoc @old-geom kill-key)) ; kill-key may be nil (no good match, new-val from new-geom). 
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
        elems  (-> (vec (interleave places trans))
                   (into places)
                   (into trans)
                   (distinct))
        angle-inc (/ (* 2 Math/PI) (count elems))]
    (with-local-vars [angle (- angle-inc)]
      (let [geom (reduce (fn [geom ename]
                           (var-set angle #(+ @angle angle-inc))
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
  (let [[x1 y1 x2 y2] (mapv double [x1 y1 x2 y2])
        slope   (/ (- y2 y1) (- x2 x1))
        y-cept  (- y1 (* slope x1))
        a (- slope)
        b 1 
        c y-cept]
    [a b c]))

(defn crossed?
  "Return true if the line segments defined by the points cross inside their length."
  [x1 y1 x2 y2 x3 y3 x4 y4]
  (let [[a b b1] (eqn x1 y1 x2 y2)
        [c d b2] (eqn x3 y3 x4 y4)
        det (- (* a d) (* b c))]
    (if (< (Math/abs det) 0.00000001)
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

