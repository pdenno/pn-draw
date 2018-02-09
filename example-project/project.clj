(defproject pdenno.example/pn-app "0.1.0-SNAPSHOT"
  :description "Example use of pn-draw"
  :url "https://github.com/pdenno/pn-draw"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure            "1.9.0"]
                 [org.clojure/clojurescript      "1.9.946"]
                 [quil                           "2.6.0"]
                 [gov.nist/spntools              "0.1.0"]
                 [pdenno/pn-draw                 "0.1.0-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]
            [lein-figwheel "0.5.14"]]
  :clean-targets ^{:protect false} ["resources/public/js/out"
                                    "resources/public/js/pn-draw.js"
                                    :target-path]
  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "pn-draw"
              :source-paths ["src"]
              :figwheel true
              :compiler {:main pdenno.pn-draw.core
                         :asset-path "js/out"
                         :output-to "resources/public/js/pn-draw.js"
                         :output-dir "resources/public/js/out"
                         :source-map-timestamp true}}]}
  :figwheel { :css-dirs ["resources/public/css"]
             :open-file-command "emacsclient"}
  :profiles {:uberjar {:aot :all}}
  :main pdenno.example.main
  :bin {:name "pn-app"
        :bin-path "~/bin" 
        :bootclasspath true
        :jvm-opts ["-server" "-Dfile.encoding=utf-8" "$JVM_OPTS" ]})




