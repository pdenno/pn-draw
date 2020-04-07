{:places
 [{:name :wc1-busy, :pid 1, :initial-tokens 1, :visible? true}
  {:name :buffer-1, :pid 2, :initial-tokens 0, :visible? true}
  {:name :wc2-busy, :pid 3, :initial-tokens 1, :visible? true}
  {:name :buffer-2, :pid 4, :initial-tokens 0, :visible? true}
  {:name :wc1-blocked, :pid 8, :initial-tokens 0, :purpose :waiting}
  {:name :wc2-blocked, :pid 9, :initial-tokens 0, :purpose :waiting}
  {:name :wc3-starved, :pid 10, :initial-tokens 0, :purpose :waiting}
  {:name :m3-busy, :pid 13, :initial-tokens 0, :purpose :waiting}
  {:name :wc2-starved, :pid 14, :initial-tokens 0, :purpose :waiting}],
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
  {:name :wc3-start-job,
   :tid 13,
   :type :immediate,
   :rate 1.0,
   :rep {:clk 600.371, :act :wc3-start-job, :m :wc3, :mjpact :sm, :bf :b2, :n 2, :j 447, :line 716},
   :visible? true}
  {:name :wc3-complete-job,
   :tid 14,
   :type :exponential,
   :rate 1.0,
   :rep {:clk 603.971, :act :wc3-complete-job, :m :wc3, :mjpact :bj, :bf :b3, :n 0, :j 447, :line 742},
   :visible? true}
  {:name :wc2-unstarve,
   :tid 14,
   :type :immediate,
   :rate 1.0,
   :rep {:clk 603.971, :act :w2-unstarve, :m :wc3, :mjpact :bj, :bf :b3, :n 0, :j 447, :line 742},
   :visible? true}
  {:name :wc2-unblock,
   :tid 14,
   :type :immediate,
   :rate 1.0,
   :rep {:clk 603.971, :act :wc2-unblock, :m :wc3, :mjpact :bj, :bf :b3, :n 0, :j 447, :line 742},
   :visible? true}],
 :arcs
 [{:aid 18, :source :wc1-start-job, :target :wc1-busy, :name :aa-18, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 19, :source :wc1-busy, :target :wc1-complete-job, :name :aa-19, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 20, :source :wc1-start-job, :target :buffer-1, :name :aa-20, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 21, :source :buffer-1, :target :wc2-start-job, :name :aa-21, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 22, :source :wc2-start-job, :target :wc2-busy, :name :aa-22, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 23, :source :wc2-busy, :target :wc2-complete-job, :name :aa-23, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 25, :source :buffer-2, :target :wc3-start-job, :name :aa-25, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 51,
   :source :wc1-complete-job,
   :target :wc1-blocked,
   :name :aa-51,
   :type :normal,
   :multiplicity 1,
   :bind {:jtype :blue},
   :priority 2}
  {:aid 52, :source :wc1-blocked, :target :wc1-start-job, :name :aa-52, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 53,
   :source :wc2-complete-job,
   :target :wc2-blocked,
   :name :aa-53,
   :type :normal,
   :multiplicity 1,
   :bind {:jtype :blue},
   :priority 2}
  {:aid 55,
   :source :wc3-complete-job,
   :target :wc3-starved,
   :name :aa-55,
   :type :normal,
   :multiplicity 1,
   :bind {:jtype :blue},
   :priority 1}
  {:aid 56, :source :wc3-starved, :target :wc3-start-job, :name :aa-56, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 59, :source :wc3-complete-job, :target :m3-busy, :name :arc-p-2, :type :normal, :multiplicity 1, :bind {:jtype :blue}, :priority 1}
  {:aid 61, :source :m3-busy, :target :wc3-start-job, :name :arc-p-4, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 156, :source :wc2-unstarve, :target :wc2-busy, :name :aa-156, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 157, :source :wc2-busy, :target :wc2-unstarve, :name :aa-157, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 158, :source :wc2-complete-job, :target :wc2-starved, :name :aa-158, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 54, :source :wc2-blocked, :target :wc2-start-job, :name :aa-54, :type :inhibitor, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 160, :source :wc2-busy, :target :wc2-start-job, :name :aa-160, :type :inhibitor, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 161, :source :wc2-starved, :target :wc2-unstarve, :name :aa-161, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 162, :source :wc2-blocked, :target :wc2-unblock, :name :aa-162, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 163, :source :wc2-unblock, :target :buffer-2, :name :aa-163, :type :normal, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 164, :source :buffer-2, :target :wc2-unblock, :name :aa-164, :type :inhibitor, :multiplicity 1, :bind {:jtype :blue}}
  {:aid 165, :source :buffer-1, :target :wc1-start-job, :name :aa-165, :type :inhibitor, :multiplicity 1, :bind {:jtype :blue}}],
 :geom
 {:wc3-starved {:x 651, :y 316, :label-x-off 12, :label-y-off 18},
  :wc3-start-job {:x 582, :y 465, :label-x-off 24, :label-y-off 1, :taken {:aa-56 9, :aa-25 0, :arc-p-4 12}, :rotate 4},
  :wc2-starved {:x 319, :y 312, :label-x-off -19, :label-y-off 26},
  :wc1-busy {:x 23, :y 326, :label-x-off 12, :label-y-off 18},
  :wc1-complete-job {:x 77, :y 200, :label-x-off -38, :label-y-off -11, :taken {:aa-19 9, :aa-51 12}},
  :wc2-unstarve {:x 269, :y 303, :label-x-off -21, :label-y-off -23, :taken {:aa-156 9, :aa-161 2, :aa-157 0}, :rotate 2},
  :wc2-busy {:x 204, :y 306, :label-x-off -49, :label-y-off -25},
  :buffer-2 {:x 450, :y 464, :label-x-off 12, :label-y-off 18},
  :m3-busy {:x 505, :y 319, :label-x-off 12, :label-y-off 18},
  :wc2-blocked {:x 422, :y 315, :label-x-off -11, :label-y-off -24},
  :wc1-blocked {:x 139, :y 329, :label-x-off -7, :label-y-off 26},
  :wc1-start-job {:x 77, :y 451, :label-x-off -32, :label-y-off 27, :taken {:aa-20 9, :aa-18 0, :aa-52 12, :aa-165 15}, :rotate 5},
  :wc2-complete-job {:x 311, :y 202, :label-x-off -45, :label-y-off -10, :taken {:aa-23 9, :aa-53 12, :aa-158 10}},
  :wc2-unblock {:x 418, :y 415, :label-x-off 10, :label-y-off -7, :taken {:aa-162 2, :aa-163 9, :aa-164 12}},
  :wc2-start-job {:x 302, :y 457, :label-x-off 12, :label-y-off 18, :taken {:aa-21 1, :aa-22 2, :aa-54 5, :aa-160 8}},
  :buffer-1 {:x 173, :y 457, :label-x-off 12, :label-y-off 18},
  :wc3-complete-job {:x 574, :y 210, :label-x-off -62, :label-y-off -9, :taken {:aa-55 9, :arc-p-2 15}}},
 :geom-arcs
 {:aa-157 {:px 217, :py 305, :tx 269, :ty 321},
  :aa-22 {:px 211, :py 317, :tx 302, :ty 453},
  :aa-164 {:px 443, :py 453, :tx 433, :ty 419},
  :aa-53 {:px 413, :py 306, :tx 326, :ty 206},
  :arc-p-4 {:px 511, :py 330, :tx 567, :ty 461},
  :aa-55 {:px 643, :py 305, :tx 574, :ty 214},
  :aa-156 {:px 217, :py 305, :tx 265, :ty 303},
  :aa-52 {:px 133, :py 341, :tx 69, :ty 437},
  :aa-163 {:px 443, :py 453, :tx 418, :ty 419},
  :arc-p-2 {:px 512, :py 308, :tx 559, :ty 214},
  :aa-161 {:px 306, :py 310, :tx 273, :ty 303},
  :aa-56 {:px 646, :py 328, :tx 582, :ty 461},
  :aa-18 {:px 28, :py 338, :tx 64, :ty 438},
  :aa-21 {:px 186, :py 457, :tx 284, :ty 457},
  :aa-160 {:px 211, :py 317, :tx 287, :ty 453},
  :aa-165 {:px 160, :py 456, :tx 91, :ty 459},
  :aa-23 {:px 213, :py 297, :tx 311, :ty 206},
  :aa-25 {:px 463, :py 464, :tx 564, :ty 465},
  :aa-19 {:px 28, :py 314, :tx 77, :ty 204},
  :aa-51 {:px 133, :py 317, :tx 92, :ty 204},
  :aa-162 {:px 421, :py 328, :tx 418, :ty 411},
  :aa-158 {:px 318, :py 299, :tx 317, :ty 206},
  :aa-20 {:px 160, :py 456, :tx 80, :ty 448},
  :aa-54 {:px 414, :py 325, :tx 317, :ty 453}}}
