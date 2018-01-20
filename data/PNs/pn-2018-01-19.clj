{:initial-marking [1 0 1 0 1 0],
 :transitions
 [{:name :m1-start-job,
   :tid 92,
   :type :exponential,
   :rate 1.0,
   :rep {:m :m1, :act :m1-start-job, :mjpact :aj, :jt :jobType2, :j 1378, :line 447, :clk 2102.3173},
   :visible? true}
  {:name :m1-complete-job,
   :tid 93,
   :type :immediate,
   :rate 1.0,
   :rep {:n 1, :m :m1, :act :m1-complete-job, :mjpact :bj, :bf :b1, :j 1378, :line 454, :clk 2103.8173},
   :visible? true}
  {:name :m2-start-job,
   :tid 94,
   :type :exponential,
   :rate 1.0,
   :rep {:n 2, :m :m2, :act :m2-start-job, :mjpact :sm, :bf :b1, :j 1378, :line 465, :clk 2105.717},
   :visible? true}
  {:name :m2-complete-job,
   :tid 95,
   :type :exponential,
   :rate 1.0,
   :rep {:n 0, :m :m2, :act :m2-complete-job, :mjpact :bj, :bf :b2, :j 1378, :line 470, :clk 2107.217},
   :visible? true}
  {:name :m3-start-job,
   :tid 96,
   :type :exponential,
   :rate 1.0,
   :rep {:n 1, :m :m3, :act :m3-start-job, :mjpact :sm, :bf :b2, :j 1378, :line 473, :clk 2107.217},
   :visible? true}
  {:name :m3-complete-job,
   :tid 97,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 2108.717, :act :m3-complete-job, :m :m3, :mjpact :ej, :ent 2102.3173, :j 1378, :line 478},
   :visible? true}],
 :arcs
 [{:aid 183, :source :m1-start-job, :target :place-2, :name :aa-183, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 184, :source :place-2, :target :m1-complete-job, :name :aa-184, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 185, :source :m1-complete-job, :target :place-3, :name :aa-185, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 186, :source :place-3, :target :m2-start-job, :name :aa-186, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 187, :source :m2-start-job, :target :place-4, :name :aa-187, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 188, :source :place-4, :target :m2-complete-job, :name :aa-188, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 189, :source :m2-complete-job, :target :place-5, :name :aa-189, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 190, :source :place-5, :target :m3-start-job, :name :aa-190, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 191, :source :m3-start-job, :target :place-6, :name :aa-191, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 192, :source :place-6, :target :m3-complete-job, :name :aa-192, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 306, :source :m3-complete-job, :target :wait-1, :name :aa-306, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 307, :source :wait-1, :target :m3-start-job, :name :aa-307, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 361, :source :place-3, :target :m1-complete-job, :name :aa-361, :type :inhibitor, :multiplicity 2}],
 :marking-key [:place-2 :place-3 :place-4 :place-5 :place-6 :wait-1],
 :places
 [{:name :place-2, :pid 0, :initial-tokens 1, :visible? true}
  {:name :place-3, :pid 1, :initial-tokens 0, :visible? true}
  {:name :place-4, :pid 2, :initial-tokens 1, :visible? true}
  {:name :place-5, :pid 3, :initial-tokens 0, :visible? true}
  {:name :place-6, :pid 4, :initial-tokens 1, :visible? true}
  {:name :wait-1, :pid 5, :initial-tokens 0, :purpose :waiting}],
 :pulls-from {:m1 [], :m2 [:place-3], :m3 [:place-5]},
 :diag-occupy-ok? true,
 :place-map {:b1 :place-3, :b2 :place-5},
 :occupy {:place-3 2, :place-5 2}}
