(ns pdenno.pn-draw.core
  "Petri net draw code"
  {:author "Peter Denno"}
  (:require [quil.core :as q]
            [quil.middleware :as qm]))

;;; ToDo: * Replace pn-trans-point: review everything on the trans and distribute
;;;         so that things are on the correct side, not overlapping, and spaced nicely.
;;;         Step 1: define crossed? DONE.
;;;       * Show :intro :elim acts (and their multiplicity).
;;;       * Integrate simulation stepping capability.  
;;;       - Prevent coincident lines. (Example in m2-j2-bas.xml) 
;;;       - Find a way that labels can get focus despite their proximity to objects.
;;;       - display multiplicities > 1
;;;       - selective redraw

(declare pn-geom)

;;;================== Stuff borrowed from pnu (not cljs)  ========================
#?(:cljs
   (defn ppp []
     (binding [cljs.pprint/*print-right-margin* 140]
       (pprint *1))))

#?(:cljs
   (defn ppprint [arg]
     (binding [cljs.pprint/*print-right-margin* 140]
       (pprint arg))))

(defn transition? [obj] (:tid obj))

(defn name2obj
  [pn name]
  (or 
   (some #(when (= name (:name %)) %) (:places pn))
   (some #(when (= name (:name %)) %) (:transitions pn))
   (some #(when (= name (:name %)) %) (:arcs pn))))

(defn arc? [obj] (:aid obj))
(defn place? [obj] (:pid obj))

(defn pn?
  "If the argument is a Petri net, return it; otherwise return false."
  [obj]
  (and (:places obj) (:transitions obj) (:arcs obj) obj))

(def +place-dia+ 26)
(def +trans-width+ 36)
(def +trans-height+ 8)
(def +trans-prefer-center?+ true)
(def +token-dia+ 4)
(def +inhibit-dia+ 10)
(def +arrowhead-length+ 12)
(def +arrowhead-angle+ "zero is on the shaft" (/ Math/PI 8.0))
(def +lock-mouse-on+ (atom nil))
(def +hilite-elem+ (atom nil))
(def +display-pn+ (atom nil)) 

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
  (let [tw (/ +trans-width+  2.0)
        th (/ +trans-height+ 2.0)
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

(defn setup-pn []
  (q/frame-rate 20)    ; FPS. 10 is good
  (q/text-font (q/create-font "DejaVu Sans" 12 true))
  (q/background 200)) ; light grey

(declare nearest-elem ref-points draw-elem draw-arc draw-tokens)
(declare arc-coords-trans-to-place! arrowhead-coords pt-from-head)
(declare angle distance cross? hilite-elem! handle-move! rotate-trans!)

(defn draw-pn []
  (when-let [pn @+display-pn+]
    (q/background 230) ; POD not sure I want to keep this.
    (q/stroke 0) ; black
    (q/fill 255) ; white
    (q/stroke-weight 1)
    (hilite-elem! pn)
    (if (q/mouse-pressed?)
      (handle-move!)
      (reset! +lock-mouse-on+ nil))
    (doseq [place (:places pn)]
      (draw-elem pn place))
    (doseq [trans (:transitions pn)]
      (draw-elem pn trans))
    (doseq [arc (:arcs pn)]
      (draw-arc pn arc))))

(def ^:private diag (atom nil))

(defn handle-move!
  "Mouse pressed: Update coordinates to move an element or its label."
  []
  (when-let [elem (or @+lock-mouse-on+ (nearest-elem @+display-pn+ [(q/mouse-x) (q/mouse-y)]))]
    (swap!
     +display-pn+
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
  "Set +hilight-elem+ and maybe +lock-mouse-on+."
  [pn]
  (let [nearest (or @+lock-mouse-on+ (nearest-elem pn [(q/mouse-x) (q/mouse-y)]))]
    (when (and nearest (q/mouse-pressed?)) (reset! +lock-mouse-on+ nearest))
    (if nearest
      (reset! +hilite-elem+ nearest)
      (reset! +hilite-elem+ nil))))

(defn nearest-elem
  "Return a element (place/trans) map indicating what was closest to the mouse.
  :label? in the map indicates it was the elem's label that was closest.
   Returns nil if nothing is close."
  [pn mxy]
  (let [[bkey min-d label?]
        (reduce
         (fn [[bkey min-d label?] [key val]]
           (let [delem  (Math/round (distance (into mxy (vector (:x val) (:y val)))))
                 dlabel (Math/round (distance (into mxy (vector (+ (:x val) (:label-x-off val))
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

(def half-tw (Math/round (/ +trans-width+ 2.0)))
(def half-th (Math/round (/ +trans-height+ 2.0)))
(def rots [0.0 (* Math/PI 0.25) (* Math/PI 0.50) (* Math/PI 0.75)])
(defn draw-trans
  "Draw a transition with rotation as indicated by :rotate."
  [name x y]
  (q/with-translation [x y] ;[(+ x half-tw) (+ y half-th)]
    (q/with-rotation [(if-let [n (-> @+display-pn+ :geom name :rotate)]
                        (nth rots n)
                        0.0)]
      (q/rect (- half-tw) (- half-th) +trans-width+ +trans-height+))))

(defn draw-elem
  "Draws a element and its label. The element map might have :label? = true
   to indicate that it is the label that needs highlighting. The actual 
   updating of the net is taken care of in handle-mouse."
  [pn elem]
  (let [n (:name elem)
        x (-> pn :geom n :x)
        y (-> pn :geom n :y)
        hilite @+hilite-elem+]
    (q/fill (if (and (= n (:name hilite)) (:label? hilite)) (q/color 255 0 0) 0))
    (q/text (name n)
            (+ x (-> pn :geom n :label-x-off))
            (+ y (-> pn :geom n :label-y-off)))
    (q/fill 0)
    (q/stroke (if (and (= (:name hilite) n) (not (:label? hilite))) (q/color 255 0 0) 0))
    (if (place? elem)
      (do
        (q/fill 255)
        (q/ellipse x y +place-dia+ +place-dia+)
        (draw-tokens (:initial-tokens elem) x y))
      ;; It's a transition
      (do (q/fill (if (= (:type elem) :immediate) 0 255))
          (draw-trans n x y)))
    (q/stroke 0)
    (q/fill 0)))

(declare draw-tkn intersect-circle)
(defn draw-tokens
  [cnt x y]
  (let [d (+ +token-dia+ 1)]
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
  (q/fill 0) ; (q/fill 0 0 255) ; blue
  (q/ellipse x y +token-dia+ +token-dia+))

(defn midpoint [[x1 y1 x2 y2]] ; POD new
  {:x (int (/ (+ x1 x2) 2.0))
   :y (int (/ (+ y1 y2) 2.0))})

(defn draw-arc
  [pn arc]
  (let [place-is-head? (place? (name2obj pn (:target arc))),
        inhibitor? (= :inhibitor (:type arc)),
        ln
        (if place-is-head? 
          (arc-coords-trans-to-place! pn (:source arc) (:target arc) (:name arc))
          (arc-coords-trans-to-place! pn (:target arc) (:source arc) (:name arc)))]
    (when-not (== 1 (:multiplicity arc))
      (let [midt (midpoint ln)]
        (q/text (str (:multiplicity arc)) (:x midt) (:y midt)))) ; POD new  
    (if inhibitor? ; head is always the transition
      (let [center (pt-from-head (:px ln) (:py ln) (:tx ln) (:ty ln) (- (/ +inhibit-dia+ 2)))
            end (pt-from-head (:px ln) (:py ln) (:tx ln) (:ty ln) (- +inhibit-dia+))]
        (q/fill 255)
        (q/stroke-weight 1)
        (q/ellipse (:x center) (:y center) +inhibit-dia+ +inhibit-dia+)
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

(defn distance
  ([x1 y1 x2 y2] (Math/sqrt (+ (Math/pow (- x1 x2) 2) (Math/pow (- y1 y2) 2))))
  ([line] (let [[x1 y1 x2 y2] line] (distance x1 y1 x2 y2))))

(defn arrowhead-coords
  "Provide coordinates for the two points at the end of the edges of arrow at (x2,y2)"
  [x1 y1 x2 y2]
  (let [len (distance x1 y1 x2 y2)
        angle (angle x1 y1 x2 y2) 
        xl (+ len (* +arrowhead-length+ (Math/cos (- Math/PI +arrowhead-angle+))))
        xr (+ len (* +arrowhead-length+ (Math/cos (+ Math/PI +arrowhead-angle+))))
        yl (* +arrowhead-length+ (Math/sin (- Math/PI +arrowhead-angle+)))
        yr (* +arrowhead-length+ (Math/sin (+ Math/PI +arrowhead-angle+)))
        lrotate (rotate xl yl angle)
        rrotate (rotate xr yr angle)]
    {:xl (+ x1 (:x lrotate)) :yl (+ y1 (:y lrotate))
     :xr (+ x1 (:x rrotate)) :yr (+ y1 (:y rrotate))}))
  
(defn angle [x1 y1 x2 y2]
  "Calculate angle from horizontal."
  (let [scale (distance x1 y1 x2 y2)]
    (when (> scale 0)
      (let [xr (/ (- x2 x1) scale)
            yr (/ (- y2 y1) scale)]
        (cond (and (>= xr 0) (>= yr 0)) (Math/acos xr),
              (and (>= xr 0) (<= yr 0)) (- (* 2.0 Math/PI) (Math/acos xr)),
              (and (<= xr 0) (>= yr 0)) (Math/acos xr)
              :else  (- (* 2.0 Math/PI) (Math/acos xr)))))))

(defn pt-from-head
  "Return a point d units beyond (or within, if negative) the line segment."
  [x1 y1 x2 y2 d]
  (let [len (distance x1 y1 x2 y2)
        ratio (/ (+ len d) len)]
    {:x (+ x1 (* ratio (- x2 x1)))
     :y (+ y1 (* ratio (- y2 y1)))}))

(declare interesect-circle pn-trans-point trans-connects)
;;; POD Of course I could have just backed off a parametric line by +place-dia+/2...
(defn arc-coords-trans-to-place!
  [pn trans place arc]
  "Return arc coordinates for argument arc (has aid)."
  (let [[tx ty px py] (ref-points pn trans place) ; both are center points
        bc (intersect-circle ; base
            (double (- tx px))
            (double (- ty py))
            0.0
            0.0
            (/ +place-dia+ 2.0))
        tc {:x1 (+ (:x1 bc) px) ; translated
            :y1 (+ (:y1 bc) py)
            :x2 (+ (:x2 bc) px)
            :y2 (+ (:y2 bc) py)}
        ;; choose closest position on transition (with consideration of other occupancy)
        {:keys [pt take]} (pn-trans-point pn trans place arc)
        [txn tyn] pt]
    ;; Reserve the place on trans
    (swap! +display-pn+
           (fn [pn] (update-in pn [:geom trans :taken]
                               #(assoc % arc take))))
    ;; choose closest intersection on circle
    (if (< (distance (:x1 tc) (:y1 tc) tx ty)
           (distance (:x2 tc) (:y2 tc) tx ty))
      {:tx txn :ty tyn :px (:x1 tc) :py (:y1 tc)}
      {:tx txn :ty tyn :px (:x2 tc) :py (:y2 tc)})))

(defn trans-connects
  "Return a vector of [x, y] being the 16 connection points on a transition"
  [pn trans]
  (let [[rx ry] (ref-points pn trans)
        rotation (or (-> @+display-pn+ :geom trans :rotate) 0)
        offsets (nth +rot-offsets+ rotation)]
    (vec (map (fn [[xoff yoff]] (vector (+ rx xoff) (+ ry yoff))) offsets))))

;;;   8----7----6----2----3----4----5
;;;   |                             |
;;;   1           (xc,yc)           0
;;;   |                             |
;;;   15---14---13---9---10---11---12
(declare basic-candidates)
(defn pn-trans-point
  "Return {:pt [x y] :take <n>] of the best place for the argument arc to 
   connect to the trans. Considers rotation and other occupancy on the trans."
  [pn trans place arc]
  (let [me-now? (-> pn :geom trans :taken arc)
        t-connects (trans-connects pn trans)]
    (if (and me-now? (not @+lock-mouse-on+)) ; did it already assigned and elements are not being moved.
      {:pt (nth t-connects me-now?) :take me-now?}
      (let [[cx cy tx ty] (ref-points pn place trans) ; these are center points
            D (zipmap (range 16) (map (fn [txy] (distance (into [cx cy] txy))) t-connects))
            gfn (fn [n] [n (get D n)])
            top-showing? (< (get D 2) (get D 9))
            left-showing? (< (get D 1) (get D 0))
            closest (first (sort (fn [[_ d1] [_ d2]] (< d1 d2)) D))
            taken-map (-> @+display-pn+ :geom trans :taken)
            taken (vals taken-map)
            not-taken? (fn [n] (or (= n (arc taken-map)) ; take by me
                                   (not (some #(= n %) taken))))
            slope (Math/abs (double (/ (- cy ty) (max (- cx tx) 0.00001))))
            y-diff (Math/abs (double (- cy ty))) ; POD draw.cljs doesn't use slope; it uses y-diff
            ;; at this point candidates is a MAP INDEX BY AN INTEGER position See also gfn.
            candidates (basic-candidates D top-showing? left-showing?)
            candidates (remove (fn [c] (some #(= (first c) %) taken)) candidates)
            candidates (sort (fn [[_ d1] [_ d2]] (< d1 d2)) candidates)
            best (cond (empty? candidates) closest
                       (and (< slope 0.3) left-showing? (not-taken? 1)) (gfn 1) ; POD see draw.cljs
                       (and (< slope 0.3) (not-taken? 0)) (gfn 0)               ; POD see draw.cljs
                       (and +trans-prefer-center?+ top-showing? (not-taken? 2)) (gfn 2)
                       (and +trans-prefer-center?+ (not top-showing?) (not-taken? 9)) (gfn 9)
                       #_(and +trans-prefer-center?+ (not-taken? 9)) #_(gfn 9) ; POD above is draw.cljs this is original
                       :else (first candidates))] ; POD 8 below for jitter is sensitive to trans size
        (if (and me-now? (< (Math/abs (- (get D me-now?) (get D (first best)))) 3)) ; POD draw.cljs has 8, not 3.
          {:pt (nth t-connects me-now?) :take me-now?} ; Don't change. Prevent jitter.
          {:pt (nth t-connects (first best)) :take (first best)})))))

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

(defn intersect-circle
  "http://mathworld.wolfram.com/Circle-LineIntersection.html"
  [x1 y1 x2 y2 r]
  (let [dx (- x2 x1)
        dy (- y2 y1)
        dr (Math/sqrt (+ (* dx dx) (* dy dy)))
        D (- (* x1 y2) (* x2 y1))
        sgnDy (if (< dy 0) -1.0 1.0)
        rootTerm (Math/sqrt (- (* r r dr dr) (* D D)))
        denom (* dr dr)]
    {:x1 (/ (+ (* D dy) (* sgnDy dx rootTerm)) denom)
     :y1 (/ (+ (- (* D dx)) (* (Math/abs dy) rootTerm)) denom)
     :x2 (/ (- (* D dy) (* sgnDy dx rootTerm)) denom)
     :y2 (/ (- (- (* D dx)) (* (Math/abs dy) rootTerm)) denom)}))

;;; The actual quil/defsketch is in client.cljs. (Otherwise it doesn't load.) 

#?(:clj  (def last-rot (atom (System/currentTimeMillis))))
#?(:cljs (def last-rot (atom (.getTime (js/Date.)))))

;;; POD fix this!
#?(:clj
   (defn pn-wheel-fn
     [_]
     (when-let [hilite @+hilite-elem+]
       (when (contains? hilite :tid)
         (when (> (- (System/currentTimeMillis) @last-rot) 250)
           (reset! last-rot (System/currentTimeMillis))
           (swap! +display-pn+
                  (fn [pn]
                    (update-in pn
                               [:geom (:name hilite)]
                               #(cond (not (contains? % :rotate)) (assoc % :rotate 1), 
                                      (= 1 (:rotate %)) (assoc % :rotate 2),
                                      (= 2 (:rotate %)) (assoc % :rotate 3),
                                      :else (dissoc % :rotate))))))))))
#?(:cljs
   (defn pn-wheel-fn
     [_]
     (when-let [hilite @+hilite-elem+]
       (when (contains? hilite :tid)
         (when (> (- (.getTime (js/Date.)) @last-rot) 250)
           (reset! last-rot (.getTime (js/Date.)))
           (swap! +display-pn+
                  (fn [pn]
                    (update-in pn
                               [:geom (:name hilite)]
                               #(cond (not (contains? % :rotate)) (assoc % :rotate 1), 
                                      (= 1 (:rotate %)) (assoc % :rotate 2),
                                      (= 2 (:rotate %)) (assoc % :rotate 3),
                                      :else (dissoc % :rotate))))))))))


;;;---------------- window/rescaling  stuff ------------------------
(def graph-window-params {:window-size {:length 900 :height 500}
                          :x-start 30 :y-start 30})

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
        height (- (:max-y range) (:min-y range))
        params graph-window-params]
    (as-> {} ?r
      (assoc ?r :scale (* 0.8 (min (/ (-> params :window-size :length) length)
                                   (/ (-> params :window-size :height) height))))
      (assoc ?r :x-off (- (:x-start params) (:min-x range)))
      (assoc ?r :y-off (- (:y-start params) (:min-y range))))))

(declare calc-new-geom match-geom best-match)
(defn pn-geom
  "Compute reasonable display placement (:geom) for the argument PN."
  [pn old-pn]
  (if (contains? old-pn :geom)
    (assoc pn :geom (match-geom pn old-pn))
    (assoc pn :geom (calc-new-geom pn))))

(defn match-geom
  "Set the geom to match positions of stuff on the screen now, as much as possible."
  [pn old-pn]
  (let [new-geom (calc-new-geom pn)
        old-geom (atom (:geom old-pn))]
    (reduce (fn [geom [key val]]
              (if-let [old-val (key @old-geom)]
                (do (swap! old-geom #(dissoc % key))
                    (assoc geom key old-val))
                (let [[kill-key new-val] (best-match pn old-pn new-geom @old-geom key)]
                  (swap! old-geom #(dissoc % kill-key)) ; kill-key may be nil (no good match, new-val from new-geom). 
                  (assoc geom key new-val))))
            {}
            new-geom)))

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
  "Modifiy :geom to fit graph-window-params"
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
        angle-inc (/ (* 2 Math/PI) (count elems))
        angle (atom (- angle-inc))
        geom (reduce (fn [geom ename]
                       (swap! angle #(+ % angle-inc))
                       (assoc geom ename
                              {:x (Math/round (* 100 (Math/cos @angle)))
                               :y (Math/round (* 100 (Math/sin @angle)))
                               :label-x-off 10
                               :label-y-off 15}))
                     {}
                     elems)]
    (rescale geom (pn-graph-scale geom))))

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
#?(:clj
   (defn show-it []
     (reset! +display-pn+ (pn-geom (load-file "data/pn2-2018-01-19.clj") nil))
     (q/defsketch best-pn ;cljs :features [:resizable :keep-on-top]
       :host "best-pn"
       :title "Best Individual"
       :features [:keep-on-top]
       ;; Smooth=2 is typical. Can't use pixel-density with js.
       :settings #(fn [] (q/smooth 2)
                    (q/pixel-density 2))
       :mouse-wheel pn-wheel-fn
       :setup setup-pn
       :draw draw-pn
       :size [900 500])))
