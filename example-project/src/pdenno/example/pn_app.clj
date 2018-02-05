(ns pdenno.example.pn-app
  "Petri net draw code"
  (:require [quil.core :as q]
            [pdenno.pn-draw.util :as util :refer :all]
            [pdenno.pn-draw.core :as pnd :refer [+display-pn+]]))

;;; Start it in a terminal with lein repl. (You can later cider-connect.)
;;; Run (show-it) from the terminal repl. 

(def new-pn (load-file "data/PNs/pn2-2018-01-19-geom.clj"))

(defn update-pn [new-pn]
  (reset! +display-pn+ (pnd/pn-geom new-pn new-pn)))

(defn show-it []
  (update-pn new-pn)
  (q/defsketch best-pn ;cljs :features [:resizable :keep-on-top]
    :host "Tryme-PN"
    :title "A Petri Net"
    :features [:keep-on-top]
    ;; Smooth=2 is typical. Can't use pixel-density with js.
    :settings #(fn [] (q/smooth 2)
                 (q/pixel-density 2))
    :mouse-wheel pnd/pn-wheel-fn
    :setup pnd/setup-pn
    :draw pnd/draw-pn
    :size [900 500]))
