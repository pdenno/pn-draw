(ns pdenno.pn-draw.util
  "Utilities for Petri net drawing code"
  (:require #?(:cljs [cljs.pprint :refer (pprint)])
            #?(:clj  [clojure.pprint :refer (pprint)])))

(defn ppp []
  (binding
      #?(:cljs [cljs.pprint/*print-right-margin* 140])
      #?(:clj  [clojure.pprint/*print-right-margin* 140])
      (pprint *1)))

(defn ppprint [arg]
  (binding
      #?(:cljs [cljs.pprint/*print-right-margin* 140])
      #?(:clj  [clojure.pprint/*print-right-margin* 140])
    (pprint arg)))

(defn transition? [obj] (:tid obj))
(defn arc? [obj] (:aid obj))
(defn place? [obj] (:pid obj))
(defn pn? [obj] (and (:places obj) (:transitions obj) (:arcs obj) obj))

(defn name2obj
  [pn name]
  (or 
   (some #(when (= name (:name %)) %) (:places pn))
   (some #(when (= name (:name %)) %) (:transitions pn))
   (some #(when (= name (:name %)) %) (:arcs pn))))

(defn distance
  ([x1 y1 x2 y2] (Math/sqrt (+ (Math/pow (- x1 x2) 2) (Math/pow (- y1 y2) 2))))
  ([line] (let [[x1 y1 x2 y2] line] (distance x1 y1 x2 y2))))

(defn angle 
  "Calculate angle from horizontal."
  [x1 y1 x2 y2]
  (let [scale (distance x1 y1 x2 y2)]
    (when (> scale 0)
      (let [xr (/ (- x2 x1) scale)
            yr (/ (- y2 y1) scale)]
        (cond (and (>= xr 0) (>= yr 0)) (Math/acos xr),
              (and (>= xr 0) (<= yr 0)) (- (* 2.0 Math/PI) (Math/acos xr)),
              (and (<= xr 0) (>= yr 0)) (Math/acos xr)
              :else  (- (* 2.0 Math/PI) (Math/acos xr)))))))

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

