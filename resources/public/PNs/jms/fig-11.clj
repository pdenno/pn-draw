{:places
 [{:name :place-2, :pid 1, :initial-tokens 1, :visible? true}
  {:name :place-3, :pid 2, :initial-tokens 0, :visible? true}
  {:name :place-4, :pid 3, :initial-tokens 1, :visible? true}
  {:name :place-5, :pid 4, :initial-tokens 0, :visible? true}
  {:name :place-6, :pid 5, :initial-tokens 1, :visible? true}
  {:name :place-7, :pid 6, :initial-tokens 0, :visible? true}
  {:name :place-8, :pid 7, :initial-tokens 1, :visible? true}
  {:name :wait-1, :pid 8, :initial-tokens 0, :purpose :waiting}
  {:name :wait-2, :pid 9, :initial-tokens 0, :purpose :waiting}
  {:name :wait-3, :pid 10, :initial-tokens 0, :purpose :waiting}
  {:name :place-p-1, :pid 12, :initial-tokens 1, :visible? true}
  {:name :place-p-2, :pid 13, :initial-tokens 0, :purpose :waiting}
  {:name :wait-4, :pid 14, :initial-tokens 0, :purpose :waiting}
  {:name :place-9, :pid 15, :initial-tokens 0, :visible? true}
  {:name :place-10, :pid 16, :initial-tokens 0, :visible? true}],
 :transitions
 [{:name :wc1-start-job,
   :tid 9,
   :type :immediate,
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
   :type :immediate,
   :rate 1.0,
   :rep {:clk 594.4003, :act :wc2-start-job, :m :wc2, :mjpact :sm, :bf :b1, :n 1, :j 447, :line 685},
   :visible? true}
  {:name :wc2-complete-job,
   :tid 12,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 595.4003, :act :wc2-complete-job, :m :wc2, :mjpact :bj, :bf :b2, :n 1, :j 447, :line 690},
   :visible? true}
  {:name :wc3-2-start-job,
   :tid 13,
   :type :immediate,
   :rate 1.0,
   :rep {:clk 600.371, :act :wc3-2-start-job, :m :wc3-2, :mjpact :sm, :bf :b2, :n 2, :j 447, :line 716},
   :visible? true}
  {:name :wc3-2-complete-job,
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
  {:name :wc3-1-start-job,
   :tid 13,
   :type :immediate,
   :rate 1.0,
   :rep {:clk 600.371, :act :wc3-1-start-job, :m :wc3-1, :mjpact :sm, :bf :b2, :n 2, :j 447, :line 716},
   :visible? true}
  {:name :wc3-1-complete-job,
   :tid 14,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 603.971, :act :wc3-1-complete-job, :m :wc3-1, :mjpact :bj, :bf :b3, :n 0, :j 447, :line 742},
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
 [{:aid 18, :source :wc1-start-job, :target :place-2, :name :aa-18, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 19, :source :place-2, :target :wc1-complete-job, :name :aa-19, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 20, :source :wc1-start-job, :target :place-3, :name :aa-20, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 21, :source :place-3, :target :wc2-start-job, :name :aa-21, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 22, :source :wc2-start-job, :target :place-4, :name :aa-22, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 23, :source :place-4, :target :wc2-complete-job, :name :aa-23, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 24, :source :wc2-complete-job, :target :place-5, :name :aa-24, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 25, :source :place-5, :target :wc3-2-start-job, :name :aa-25, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 26, :source :wc3-2-start-job, :target :place-6, :name :aa-26, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 27, :source :place-6, :target :wc3-2-complete-job, :name :aa-27, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 28, :source :wc3-2-complete-job, :target :place-7, :name :aa-28, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 29, :source :place-7, :target :wc4-start-job, :name :aa-29, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 30, :source :wc4-start-job, :target :place-8, :name :aa-30, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 31, :source :place-8, :target :wc4-complete-job, :name :aa-31, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 51, :source :wc1-complete-job, :target :wait-1, :name :aa-51, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 52, :source :wait-1, :target :wc1-start-job, :name :aa-52, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 53, :source :wc2-complete-job, :target :wait-2, :name :aa-53, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 54, :source :wait-2, :target :wc2-start-job, :name :aa-54, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 55, :source :wc3-2-complete-job, :target :wait-3, :name :aa-55, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 56, :source :wait-3, :target :wc3-2-start-job, :name :aa-56, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 58, :source :place-p-1, :target :wc3-1-complete-job, :name :arc-p-1, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 59,
   :source :wc3-1-complete-job,
   :target :place-p-2,
   :name :arc-p-2,
   :type :normal,
   :multiplicity 1,
   :bind {:jtype :blue},
   :priority 1}
  {:aid 60,
   :source :wc3-1-start-job,
   :target :place-p-1,
   :name :arc-p-3,
   :type :normal,
   :multiplicity 1,
   :bind {:jtype :blue},
   :priority 1}
  {:aid 61, :source :place-p-2, :target :wc3-1-start-job, :name :arc-p-4, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 155, :source :place-5, :target :wc3-1-start-job, :name :aa-155, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 156, :source :wc3-1-complete-job, :target :place-7, :name :aa-156, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 157, :source :wc4-complete-job, :target :wait-4, :name :aa-157, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 158, :source :wait-4, :target :wc4-start-job, :name :aa-158, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 159, :source :wc4-start-job, :target :place-9, :name :aa-159, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 160, :source :place-9, :target :wc5-start-job, :name :aa-160, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 161, :source :wc5-start-job, :target :place-10, :name :aa-161, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 162, :source :place-10, :target :wc5-complete-job, :name :aa-162, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 163, :source :place-3, :target :wc1-start-job, :name :aa-162, :type :inhibitor, :multiplicity 2}
  {:aid 164, :source :place-5, :target :wc2-start-job, :name :aa-162, :type :inhibitor, :multiplicity 2}
  {:aid 165, :source :place-7, :target :wc3-1-start-job, :name :aa-162, :type :inhibitor, :multiplicity 1}
  {:aid 166, :source :place-7, :target :wc3-2-start-job, :name :aa-162, :type :inhibitor, :multiplicity 1}],
 :geom
 {:wc3-1-start-job {:x 320, :y 359, :label-x-off 12, :label-y-off 18},
  :place-9 {:x 730, :y 469, :label-x-off -7, :label-y-off -22},
  :wc3-2-complete-job {:x 552, :y 113, :label-x-off -25, :label-y-off -10},
  :wait-4 {:x 729, :y 378, :label-x-off 12, :label-y-off 18},
  :wc1-complete-job {:x 29, :y 262, :label-x-off -16, :label-y-off -13},
  :wc4-complete-job {:x 697, :y 254, :label-x-off -65, :label-y-off -10},
  :place-p-2 {:x 396, :y 294, :label-x-off -18, :label-y-off 27},
  :place-4 {:x 157, :y 355, :label-x-off -45, :label-y-off -20},
  :wc5-start-job {:x 824, :y 467, :label-x-off -14, :label-y-off 20},
  :place-6 {:x 458, :y 259, :label-x-off -38, :label-y-off -15},
  :wc3-1-complete-job {:x 411, :y 114, :label-x-off -80, :label-y-off -10},
  :wc1-start-job {:x 22, :y 464, :label-x-off -10, :label-y-off 26},
  :place-10 {:x 820, :y 375, :label-x-off 9, :label-y-off -16},
  :wc2-complete-job {:x 197, :y 265, :label-x-off -18, :label-y-off -16},
  :wait-2 {:x 205, :y 409, :label-x-off 12, :label-y-off 18},
  :wc5-complete-job {:x 815, :y 254, :label-x-off -40, :label-y-off -13},
  :wait-1 {:x 104, :y 366, :label-x-off -1, :label-y-off 24},
  :wc2-start-job {:x 156, :y 467, :label-x-off 12, :label-y-off 18},
  :wait-3 {:x 553, :y 298, :label-x-off -41, :label-y-off -17},
  :wc4-start-job {:x 658, :y 469, :label-x-off -15, :label-y-off 22},
  :place-p-1 {:x 322, :y 177, :label-x-off -39, :label-y-off -21},
  :wc3-2-start-job {:x 481, :y 367, :label-x-off -17, :label-y-off 28},
  :place-3 {:x 93, :y 465, :label-x-off -19, :label-y-off -17},
  :place-5 {:x 284, :y 469, :label-x-off 23, :label-y-off 0},
  :place-2 {:x 25, :y 359, :label-x-off 12, :label-y-off 18},
  :place-7 {:x 589, :y 468, :label-x-off -60, :label-y-off -4},
  :place-8 {:x 655, :y 372, :label-x-off -51, :label-y-off -18}}}
