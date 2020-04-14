{:places
 [{:name :m1-busy, :pid 1, :initial-tokens 1, :visible? true}
  {:name :m1-blocked, :pid 2, :initial-tokens 0, :visible? true}
  {:name :buffer, :pid 3, :initial-tokens 0, :visible? true}
  {:name :m2-busy, :pid 4, :initial-tokens 1, :visible? true}
  {:name :m2-starved, :pid 5, :initial-tokens 0, :visible? true}],
 :transitions
 [{:name :m1-start-job, :tid 6, :type :immediate, :visible? true}
  {:name :m1-complete-job,
   :tid 7,
   :type :exponential,
   :rate 1.0,
   :rep
   {:clk 594.1681,
    :act :wc1-complete-job,
    :m :wc1,
    :mjpact :bj,
    :bf :b1,
    :n 0,
    :j 447,
    :line 676},
   :visible? true}
  {:name :m2-complete-job,
   :tid 8,
   :type :exponential,
   :rate 1.0,
   :rep
   {:clk 593.1681,
    :act :wc2-start-job,
    :m :wc1,
    :mjpact :aj,
    :jt :jobType2,
    :j 447,
    :line 672},
   :visible? true}
  {:name :m2-start-job, :tid 9, :type :immediate, :visible? true}],
 :arcs
 [{:aid 10,
   :source :m1-start-job,
   :target :m1-busy,
   :name :aa-18,
   :type :normal,
   :multiplicity 1}
  {:aid 11,
   :source :m1-busy,
   :target :m1-complete-job,
   :name :aa-19,
   :type :normal,
   :multiplicity 1}
  {:aid 12,
   :source :m1-complete-job,
   :target :m1-blocked,
   :name :aa-20,
   :type :normal,
   :multiplicity 1}
  {:aid 13,
   :source :m1-complete-job,
   :target :buffer,
   :name :aa-21,
   :type :normal,
   :multiplicity 1}
  {:aid 14,
   :source :m1-blocked,
   :target :m1-start-job,
   :name :aa-22,
   :type :normal,
   :multiplicity 1}
  {:aid 15,
   :source :buffer,
   :target :m1-start-job,
   :name :aa-23,
   :type :inhibitor,
   :multiplicity 1}
  {:aid 16,
   :source :buffer,
   :target :m2-start-job,
   :name :aa-24,
   :type :normal,
   :multiplicity 1}
  {:aid 17,
   :source :m2-start-job,
   :target :m2-busy,
   :name :aa-25,
   :type :normal,
   :multiplicity 1}
  {:aid 18,
   :source :m2-busy,
   :target :m2-complete-job,
   :name :aa-25,
   :type :normal,
   :multiplicity 1}
  {:aid 19,
   :source :m2-complete-job,
   :target :m2-starved,
   :name :aa-26,
   :type :normal,
   :multiplicity 1}
  {:aid 20,
   :source :m2-starved,
   :target :m2-start-job,
   :name :aa-27,
   :type :normal,
   :multiplicity 1}],
 :geom
 {:m1-complete-job
  {:x 168,
   :y 206,
   :label-x-off -4,
   :label-y-off -17,
   :taken {:aa-19 2, :aa-20 9, :aa-21 12}},
  :m2-starved {:x 403, :y 290, :label-x-off 12, :label-y-off 18},
  :m1-start-job
  {:x 71,
   :y 373,
   :label-x-off -11,
   :label-y-off 41,
   :taken {:aa-23 15, :aa-18 9, :aa-22 13},
   :rotate 5},
  :m1-busy {:x 69, :y 107, :label-x-off 30, :label-y-off 7},
  :m1-blocked {:x 130, :y 322, :label-x-off 12, :label-y-off 18},
  :m2-start-job
  {:x 390,
   :y 384,
   :label-x-off 32,
   :label-y-off 0,
   :taken {:aa-24 1, :aa-25 2, :aa-27 4}},
  :m2-busy {:x 288, :y 119, :label-x-off -83, :label-y-off -6},
  :buffer {:x 282, :y 384, :label-x-off 10, :label-y-off 26},
  :m2-complete-job
  {:x 402,
   :y 204,
   :label-x-off 12,
   :label-y-off 18,
   :taken {:aa-25 2, :aa-26 9}}},
 :geom-arcs
 {:aa-22 {:px 120, :py 331, :tx 78, :ty 374},
  :aa-24 {:px 295, :py 384, :tx 372, :ty 384},
  :aa-27 {:px 401, :py 303, :tx 402, :ty 380},
  :aa-26 {:px 403, :py 277, :tx 402, :ty 208},
  :aa-18 {:px 69, :py 120, :tx 74, :ty 370},
  :aa-21 {:px 275, :py 373, :tx 183, :ty 210},
  :aa-23 {:px 269, :py 383, :tx 85, :ty 381},
  :aa-25 {:px 298, :py 127, :tx 402, :ty 200},
  :aa-19 {:px 78, :py 116, :tx 168, :ty 202},
  :aa-20 {:px 134, :py 310, :tx 168, :ty 210}}}
