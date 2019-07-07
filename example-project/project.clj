(defproject pdenno.example/pn-app "0.1.0-SNAPSHOT"
  :description "Example use of pn-draw"
  :url "https://github.com/pdenno/pn-draw"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure            "1.10.1"]
                 [org.clojure/clojurescript      "1.10.520"]
                 [quil                           "3.0.0"]
                 [pdenno/spntools                "0.1.0"]
                 [pdenno/pn-draw                 "0.1.0-SNAPSHOT"]]
  :profiles {:uberjar {:aot :all}}
  :main pdenno.example.pn-app
  :bin {:name "pn-app"
        :bin-path "~/bin" 
        :bootclasspath true
        :jvm-opts ["-server" "-Dfile.encoding=utf-8" "$JVM_OPTS" ]})




