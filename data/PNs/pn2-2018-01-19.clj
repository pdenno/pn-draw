;;; 3-machine with m1 is BAS, m2 is BBS
{:places
 [{:name :place-2, :pid 189, :initial-tokens 0, :visible? true}
  {:name :place-3, :pid 190, :initial-tokens 0, :visible? true}
  {:name :place-4, :pid 191, :initial-tokens 0, :visible? true}
  {:name :place-5, :pid 192, :initial-tokens 0, :visible? true}
  {:name :place-6, :pid 193, :initial-tokens 0, :visible? true}
  {:name :wait-1, :pid 194, :initial-tokens 0, :purpose :waiting}
  {:name :wait-2, :pid 195, :initial-tokens 0, :purpose :waiting}
  {:name :wait-3, :pid 196, :initial-tokens 0, :purpose :waiting}],
 :transitions
 [{:name :m1-start-job,
   :tid 164,
   :type :exponential,
   :rate 1.0,
   :rep {:m :m1, :act :m1-start-job, :mjpact :aj, :jt :jobType2, :j 1620, :line 2005, :clk 2458.4463},
   :visible? true}
  {:name :m1-complete-job,
   :tid 165,
   :type :exponential,
   :rate 1.0,
   :rep {:n 0, :m :m1, :act :m1-complete-job, :mjpact :bj, :bf :b1, :j 1620, :line 2010, :clk 2459.9463},
   :visible? true}
  {:name :m2-start-job,
   :tid 166,
   :type :exponential,
   :rate 1.0,
   :rep {:n 1, :m :m2, :act :m2-start-job, :mjpact :sm, :bf :b1, :j 1620, :line 2013, :clk 2459.9463},
   :visible? true}
  {:name :m2-complete-job,
   :tid 167,
   :type :exponential,
   :rate 1.0,
   :rep {:n 0, :m :m2, :act :m2-complete-job, :mjpact :bj, :bf :b2, :j 1620, :line 2022, :clk 2463.804},
   :visible? true}
  {:name :m3-start-job,
   :tid 168,
   :type :exponential,
   :rate 1.0,
   :rep {:n 1, :m :m3, :act :m3-start-job, :mjpact :sm, :bf :b2, :j 1620, :line 2025, :clk 2463.804},
   :visible? true}
  {:name :m3-complete-job,
   :tid 169,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 2465.304, :act :m3-complete-job, :m :m3, :mjpact :ej, :ent 2458.4463, :j 1620, :line 2029},
   :visible? true}],
 :arcs
 [{:aid 423, :source :m1-start-job, :target :place-2, :name :aa-423, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 424, :source :place-2, :target :m1-complete-job, :name :aa-424, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 425, :source :m1-start-job, :target :place-3, :name :aa-425, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 426, :source :place-3, :target :m2-start-job, :name :aa-426, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 427, :source :m2-start-job, :target :place-4, :name :aa-427, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 428, :source :place-4, :target :m2-complete-job, :name :aa-428, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 429, :source :m2-complete-job, :target :place-5, :name :aa-429, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 430, :source :place-5, :target :m3-start-job, :name :aa-430, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 431, :source :m3-start-job, :target :place-6, :name :aa-431, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 432, :source :place-6, :target :m3-complete-job, :name :aa-432, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 434, :source :m1-complete-job, :target :wait-1, :name :aa-434, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 435, :source :wait-1, :target :m1-start-job, :name :aa-435, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 436, :source :m2-complete-job, :target :wait-2, :name :aa-436, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 437, :source :wait-2, :target :m2-start-job, :name :aa-437, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 438, :source :m3-complete-job, :target :wait-3, :name :aa-438, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 439, :source :wait-3, :target :m3-start-job, :name :aa-439, :type :normal, :multiplicity 1, :bind {:jtype :blue}}]}
