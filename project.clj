(defproject essen-basket "0.0.4"
  :description "essen-basket : Essential trading basket/order reader."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.6.1"]
                 [enlive "1.1.1"]
                 [org.clojure/tools.cli "0.3.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}
             :uberjar {:aot :all}}
  :uberjar-name "essen-basket.jar"
  :main ^:skip-aot essen-basket.core
  :target-path "target/%s")
