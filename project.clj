(defproject essbasket "0.0.1-SNAPSHOT"
  :description "essen-basket : Essential trading basket reader."
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.2"]
                 [enlive "1.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}})
