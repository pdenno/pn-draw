;;; Example PN with :geom 
{:places
 [{:name :place-2, :pid 189, :initial-tokens 1, :visible? true}
  {:name :place-3, :pid 190, :initial-tokens 0, :visible? true}
  {:name :place-4, :pid 191, :initial-tokens 1, :visible? true}
  {:name :place-5, :pid 192, :initial-tokens 0, :visible? true}
  {:name :place-6, :pid 193, :initial-tokens 1, :visible? true}
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
  {:aid 425, :source :m1-start-job, :target :place-3, :name :aa-425, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 426, :source :place-3, :target :m2-start-job, :name :aa-426, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 427, :source :m2-start-job, :target :place-4, :name :aa-427, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 428, :source :place-4, :target :m2-complete-job, :name :aa-428, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 429, :source :m2-complete-job, :target :place-5, :name :aa-429, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 2}
  {:aid 430, :source :place-5, :target :m3-start-job, :name :aa-430, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 431, :source :m3-start-job, :target :place-6, :name :aa-431, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 432, :source :place-6, :target :m3-complete-job, :name :aa-432, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 434, :source :m1-complete-job, :target :wait-1, :name :aa-434, :type :normal, :multiplicity 1, :bind {:jtype :blue} :priority 1}
  {:aid 435, :source :wait-1, :target :m1-start-job, :name :aa-435, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 436, :source :m2-complete-job, :target :wait-2, :name :aa-436, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 437, :source :wait-2, :target :m2-start-job, :name :aa-437, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 438, :source :m3-complete-job, :target :wait-3, :name :aa-438, :type :normal, :multiplicity 1, :bind {:jtype :blue} :priority 1}
  {:aid 439, :source :wait-3, :target :m3-start-job, :name :aa-439, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 440, :source :place-3, :target :m1-start-job, :name :aa-440, :type :inhibitor, :multiplicity 3, :bind {:jtype :blue}}],
 :geom
 {:m3-complete-job {:x 461, :y 186, :label-x-off -19, :label-y-off -16},
  :m1-complete-job {:x 89, :y 177, :label-x-off -12, :label-y-off -14},
  :m3-start-job {:x 459, :y 451, :label-x-off 12, :label-y-off 19},
  :place-4 {:x 194, :y 269, :label-x-off -46, :label-y-off -20},
  :place-6 {:x 421, :y 294, :label-x-off -56, :label-y-off -16},
  :m1-start-job {:x 56, :y 427, :label-x-off -7, :label-y-off 20},
  :wait-2 {:x 260, :y 323, :label-x-off 12, :label-y-off 19},
  :m2-start-job {:x 256, :y 438, :label-x-off -2, :label-y-off 20},
  :wait-1 {:x 77, :y 311, :label-x-off 12, :label-y-off 19},
  :wait-3 {:x 513, :y 347, :label-x-off 21, :label-y-off 10},
  :place-3 {:x 171, :y 432, :label-x-off 12, :label-y-off 19},
  :place-5 {:x 372, :y 442, :label-x-off 12, :label-y-off 19},
  :place-2 {:x 33, :y 255, :label-x-off -20, :label-y-off -27},
  :m2-complete-job {:x 237, :y 179, :label-x-off -16, :label-y-off -16}}}
