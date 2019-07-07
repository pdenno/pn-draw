(ns pdenno.example.pn-app
  "Petri net draw code"
  (:require [quil.core :as q]
            [pdenno.pn-draw.util :as util]
            [pdenno.pn-draw.core :as pnd]))

;;; Start it in a terminal with lein repl. (You can later cider-connect.)
;;; Run (show-it) from the terminal repl. 

(defn show-it []
  (dosync (reset! pnd/the-pn
                   (-> "resources/public/PNs/pn2-2018-01-19-geom.clj"
                       load-file
                       pnd/pn-geom)))
  (q/defsketch best-pn ;cljs :features [:resizable :keep-on-top]
    :host "Tryme-PN"
    :title "A Petri Net"
    :features [:keep-on-top]
    ;; Smooth=2 is typical. Can't use pixel-density with js.
    :settings #(fn []
                 (q/smooth 2)
                 (q/pixel-density 2))
    :mouse-wheel pnd/pn-wheel-fn!
    :setup pnd/setup-pn
    :draw pnd/draw-pn
    :size [900 500]))
