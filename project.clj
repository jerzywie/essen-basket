(defproject essen-basket "0.0.4-SNAPSHOT"
  :description "essen-basket : Essential trading basket/order reader."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.2"]
                 [enlive "1.1.1"]
                 [org.clojure/tools.cli "0.3.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}
             :uberjar {:aot :all}}
  :main ^:skip-aot essen-basket.core
  :target-path "target/%s")
