(ns essen-basket.core
  (:require [essen-basket.sitehandling  :as site])
  (:require [essen-basket.scrape        :as scrape]))


(defn scrape-basket
  "Scrapes basket off Essential site"
  [username password]

  (try
   (println "doing login")
   (site/perform-login (site/get-login-vars username password))
   (println "getting basket page 1")
   (let [page1 (site/get-basket-page1)]
     (println "looping over next pages")
     (loop [order-rows (scrape/scrape-order (:body page1))
            next-page-vars (site/get-basket-next-page-vars (:body page1))]
       (if (nil? next-page-vars)
         (sort order-rows)
         (let  [resp (site/get-basket-next-page next-page-vars)]
           (recur (into order-rows (scrape/scrape-order (:body resp)))
                  (site/get-basket-next-page-vars (:body resp)))))))
   (catch Exception e
     (println (str "Error: " (.getMessage e) " Stacktrace: " (.getStackTrace e)))
     (.printStackTrace e))))


(defn save-basket-to-file
  "Saves the basket rows to a text file in tsv format"
  [file username password]
 (scrape/write-file file (scrape-basket username password)))
