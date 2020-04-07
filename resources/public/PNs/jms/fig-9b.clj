{:places
 [{:name :place-1, :pid 1, :initial-tokens 1, :visible? true}
  {:name :wait-1,  :pid 2, :initial-tokens 0, :visible? true}
  {:name :place-2, :pid 3, :initial-tokens 0, :visible? true}],
 :transitions
 [{:name :wc1-start-job,
   :tid 9,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 593.1681, :act :wc1-start-job, :m :wc1, :mjpact :aj, :jt :jobType2, :j 447, :line 672},
   :visible? true}
  {:name :wc1-complete-job,
   :tid 10,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 594.1681, :act :wc1-complete-job, :m :wc1, :mjpact :bj, :bf :b1, :n 0, :j 447, :line 676},
   :visible? true}],
 :arcs
 [{:aid 18, :source :wc1-start-job, :target :place-1, :name :aa-18, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 19, :source :place-1, :target :wc1-complete-job, :name :aa-19, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 21, :source :wc1-complete-job, :target :wait-1, :name :aa-19, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 22, :source :wait-1, :target :wc1-start-job, :name :aa-19, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 23, :source :wc1-start-job, :target :place-2, :name :aa-20, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}]}
