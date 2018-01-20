;;; This is 3-machine with all BBS and simple "wait" 
{:places
 [{:name :place-2, :pid 207, :initial-tokens 0, :visible? true}
  {:name :place-3, :pid 208, :initial-tokens 0, :visible? true}
  {:name :place-4, :pid 209, :initial-tokens 0, :visible? true}
  {:name :place-5, :pid 210, :initial-tokens 0, :visible? true}
  {:name :place-6, :pid 211, :initial-tokens 0, :visible? true}
  {:name :wait-1, :pid 212, :initial-tokens 0, :purpose :waiting}
  {:name :wait-2, :pid 213, :initial-tokens 0, :purpose :waiting}
  {:name :wait-3, :pid 214, :initial-tokens 0, :purpose :waiting}],
 :transitions
 [{:name :m1-start-job,
   :tid 176,
   :type :exponential,
   :rate 1.0,
   :rep {:m :m1, :act :m1-start-job, :mjpact :aj, :jt :jobType1, :j 1444, :line 873, :clk 2191.6082},
   :visible? true}
  {:name :m1-complete-job,
   :tid 177,
   :type :exponential,
   :rate 1.0,
   :rep {:n 0, :m :m1, :act :m1-complete-job, :mjpact :bj, :bf :b1, :j 1444, :line 878, :clk 2192.6082},
   :visible? true}
  {:name :m2-start-job,
   :tid 178,
   :type :exponential,
   :rate 1.0,
   :rep {:n 2, :m :m2, :act :m2-start-job, :mjpact :sm, :bf :b1, :j 1444, :line 885, :clk 2195.2146},
   :visible? true}
  {:name :m2-complete-job,
   :tid 179,
   :type :exponential,
   :rate 1.0,
   :rep {:n 1, :m :m2, :act :m2-complete-job, :mjpact :bj, :bf :b2, :j 1444, :line 887, :clk 2196.2146},
   :visible? true}
  {:name :m3-start-job,
   :tid 180,
   :type :exponential,
   :rate 1.0,
   :rep {:n 2, :m :m3, :act :m3-start-job, :mjpact :sm, :bf :b2, :j 1444, :line 903, :clk 2202.0433},
   :visible? true}
  {:name :m3-complete-job,
   :tid 181,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 2203.0433, :act :m3-complete-job, :m :m3, :mjpact :ej, :ent 2191.6082, :j 1444, :line 906},
   :visible? true}],
 :arcs
 [{:aid 459, :source :m1-start-job, :target :place-2, :name :aa-459, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 460, :source :place-2, :target :m1-complete-job, :name :aa-460, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 461, :source :m1-complete-job, :target :place-3, :name :aa-461, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 462, :source :place-3, :target :m2-start-job, :name :aa-462, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 463, :source :m2-start-job, :target :place-4, :name :aa-463, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 464, :source :place-4, :target :m2-complete-job, :name :aa-464, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 465, :source :m2-complete-job, :target :place-5, :name :aa-465, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 466, :source :place-5, :target :m3-start-job, :name :aa-466, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 467, :source :m3-start-job, :target :place-6, :name :aa-467, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 468, :source :place-6, :target :m3-complete-job, :name :aa-468, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 470, :source :m1-complete-job, :target :wait-1, :name :aa-470, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 471, :source :wait-1, :target :m1-start-job, :name :aa-471, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 472, :source :m2-complete-job, :target :wait-2, :name :aa-472, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 473, :source :wait-2, :target :m2-start-job, :name :aa-473, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 474, :source :m3-complete-job, :target :wait-3, :name :aa-474, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 475, :source :wait-3, :target :m3-start-job, :name :aa-475, :type :normal, :multiplicity 1, :bind {:jtype :blue}}]}
