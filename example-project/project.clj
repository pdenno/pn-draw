(defproject pdenno.example/pn-app "0.1.0-SNAPSHOT"
  :description "Example use of pn-draw"
  :url "https://github.com/pdenno/pn-draw"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure       "1.9.0"]
                 [gov.nist/spntools         "0.1.0-SNAPSHOT"]
                 [pdenno/pn-draw            "0.1.0-SNAPSHOT"]
                 [quil                      "2.6.0"]]
  :main ^:skip-aot pdenno.example.pn-app) ; Without this, it doesn't load that file. 



