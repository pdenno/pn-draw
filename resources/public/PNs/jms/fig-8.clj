{:places
 [{:name :place-1, :pid 0, :initial-tokens 1, :visible? true}
  {:name :place-2, :pid 1, :initial-tokens 0, :visible? true}
  {:name :place-3, :pid 2, :initial-tokens 0, :visible? true}
  {:name :place-4, :pid 3, :initial-tokens 0, :visible? true}
  {:name :place-5, :pid 4, :initial-tokens 0, :visible? true}
  {:name :place-6, :pid 5, :initial-tokens 0, :visible? true}
  {:name :place-7, :pid 6, :initial-tokens 0, :visible? true}
  {:name :place-8, :pid 7, :initial-tokens 0, :visible? true}
  {:name :place-9, :pid 15, :initial-tokens 0, :visible? true}],
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
   :visible? true}
  {:name :wc2-start-job,
   :tid 11,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 594.4003, :act :wc2-start-job, :m :wc2, :mjpact :sm, :bf :b1, :n 1, :j 447, :line 685},
   :visible? true}
  {:name :wc2-complete-job,
   :tid 12,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 595.4003, :act :wc2-complete-job, :m :wc2, :mjpact :bj, :bf :b2, :n 1, :j 447, :line 690},
   :visible? true}
  {:name :wc3-1-start-job,
   :tid 13,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 600.371, :act :wc3-2-start-job, :m :wc3-2, :mjpact :sm, :bf :b2, :n 2, :j 447, :line 716},
   :visible? true}
  {:name :wc3-1-complete-job,
   :tid 14,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 603.971, :act :wc3-2-complete-job, :m :wc3-2, :mjpact :bj, :bf :b3, :n 0, :j 447, :line 742},
   :visible? true}
  {:name :wc4-start-job,
   :tid 15,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 604.771, :act :wc4-start-job, :m :wc4, :mjpact :sm, :bf :b3, :n 1, :j 447, :line 747},
   :visible? true}
  {:name :wc4-complete-job,
   :tid 16,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 605.971, :act :wc4-complete-job, :m :wc4, :mjpact :ej, :ent 593.1681, :j 447, :line 751},
   :visible? true}
  {:name :wc5-start-job,
   :tid 17,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 607.771, :act :wc5-start-job, :m :wc5, :mjpact :sm, :bf :b3, :n 1, :j 447, :line 754},
   :visible? true}
  {:name :wc5-complete-job,
   :tid 18,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 608.971, :act :wc5-complete-job, :m :wc5, :mjpact :ej, :ent 593.1681, :j 447, :line 755},
   :visible? true}],
 :arcs
 [{:aid 18, :source :wc1-start-job, :target :place-1, :name :aa-18, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 19, :source :place-1, :target :wc1-complete-job, :name :aa-19, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 20, :source :wc1-complete-job, :target :place-2, :name :aa-20, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 21, :source :place-2, :target :wc2-start-job, :name :aa-21, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 22, :source :wc2-start-job, :target :place-3, :name :aa-22, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 23, :source :place-3, :target :wc2-complete-job, :name :aa-23, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 24, :source :wc2-complete-job, :target :place-4, :name :aa-24, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 25, :source :place-4, :target :wc3-1-start-job, :name :aa-25, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 26, :source :wc3-1-start-job, :target :place-5, :name :aa-26, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 27, :source :place-5, :target :wc3-1-complete-job, :name :aa-27, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 28, :source :wc3-1-complete-job, :target :place-6, :name :aa-28, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 29, :source :place-6, :target :wc4-start-job, :name :aa-29, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 30, :source :wc4-start-job, :target :place-7, :name :aa-30, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 31, :source :place-7, :target :wc4-complete-job, :name :aa-31, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 51, :source :wc4-complete-job, :target :place-8, :name :aa-51, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 52, :source :place-8, :target :wc5-start-job, :name :aa-52, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 53, :source :wc5-start-job, :target :place-9, :name :aa-53, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 54, :source :place-9, :target :wc5-complete-job, :name :aa-54, :type :normal, :multiplicity 1, :bind {:jtype :blue}}]}
