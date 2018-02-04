(ns pdenno.pn-draw.simulate-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer (cl-format pprint pp)]
            [gov.nist.spntools.core :as spn]
            [gov.nist.spntools.util.reach :as pnr]
            [gov.nist.spntools.util.utils :as pnu]
            [gov.nist.spntools.util.pnml  :as pnml]
            [pdenno.pn-draw.simulate :as sim]))

(defn diag-force-priority
  "Set PN priority as indicated by the argument. Anything not specified has priority=1."
  [pn priority-maps]
  (as-> pn ?pn
    (update ?pn :arcs (fn [arcs] (vec (map #(assoc % :priority 1) arcs))))
    (update ?pn :arcs
            (fn [arcs] (reduce (fn [arcs pr]
                                 (if-let [ar (some #(when (and (= (:source %) (:source pr))
                                                               (= (:target %) (:target pr)))
                                                      %)
                                                   arcs)]
                                   (assoc-in arcs [(pnu/arc-index pn (:name ar)) :priority] (:priority pr))
                                   arcs))
                               arcs priority-maps)))))

(defn add-color-binding
  "Add color binding information."
  [pn]
  (update-in pn [:arcs] (fn [arcs] (vec (map #(assoc % :bind {:jtype :blue}) arcs)))))

(defn m2-inhib-bas-sim
  "Setup the m2-inhib-bas PN for a simulation."
  [steps]
  (-> "data/PNs/m2-inhib-bas.xml" 
      spn/run-ready
      add-color-binding
      diag-force-priority [{:source :m1-start-job, :target :buffer :priority 2}])
      (sim/simulate :max-steps steps))

(defn sequence? [seq]
  "Returns true if SEQ is a sequence of integers in order [n, n+1, n+2,...,n+m]"
  (first
   (reduce (fn [[answer prev] nxt]
             (if answer
               [(== (inc prev) nxt) nxt]
               [false nil]))
           [true (first seq)]
           (rest seq))))

(deftest m2-inhib-bas-processes-serially
  (testing "serial processing of a simple PN"
    (is (->> (m2-inhib-bas-sim 200)
             :sim 
             :log
             (filter #(= :remove (:motion %)))
             (map :tkn)
             (map :id)
             sequence?))))
      
    



