(defproject essen-basket "0.0.3"
  :description "essen-basket : Essential trading basket reader."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.2"]
                 [enlive "1.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}}
  :main essen-basket.core)
