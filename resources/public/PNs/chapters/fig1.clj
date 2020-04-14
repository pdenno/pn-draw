{:places
 [{:name :p1, :pid 0, :initial-tokens 1, :visible? true}
  {:name :p2, :pid 1, :initial-tokens 0, :visible? true}
  {:name :p3, :pid 2, :initial-tokens 0, :visible? true}
  {:name :p4, :pid 3, :initial-tokens 0, :visible? true}]
 :transitions
 [{:name :t1,
   :tid 1,
;   :type :exponential,
;   :rate 1.0,
;   :rep {:clk 593.1681, :act :wc1-start-job, :m :wc1, :mjpact :aj, :jt :jobType2, :j 447, :line 672},
   :type :immediate,
   :visible? true}],
 :arcs
 [{:aid 1, :source :p1, :target :t1, :name :a1, :type :normal, :multiplicity 1}
  {:aid 2, :source :t1, :target :p2, :name :a2, :type :normal, :multiplicity 1}
  {:aid 3, :source :t1, :target :p3, :name :a3, :type :normal, :multiplicity 2}
  {:aid 3, :source :p4, :target :t1, :name :a4, :type :inhibitor, :multiplicity 1}]}
