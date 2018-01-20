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

