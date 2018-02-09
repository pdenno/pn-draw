(ns pdenno.pn-draw.core-test
  (:require [clojure.test :refer :all]
            [quil.core :as q]
            [quil.middleware :as qm]
            [pdenno.pn-draw.core :as core :refer :all]))

;;; Also see the code in example-project/
(defn show-it []
  (let [pn (load-file "resources/public/PNs/pn2-2018-01-19-geom.clj")]
    (reset! core/the-pn (core/pn-geom pn))
    (q/defsketch best-pn 
      :host "Tryme-PN"
      :title "A Petri Net"
      :features [:keep-on-top]
      ;; Smooth=2 is typical. Can't use pixel-density with js.
      :settings #(fn [] (q/smooth 2)) 
      :setup core/setup-pn
      :draw core/draw-pn
      :mouse-wheel core/pn-wheel-fn
      :size [(:window-length core/params)
             (:window-height core/params)])))
  
