(ns essen-basket.core
  (:require [essen-basket.sitehandling  :as site]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.java.io        :as io])
  (:gen-class :main true))

(def cli-options
  [;; First three strings describe a short-option, long-option with optional
   ;; example argument description, and a description. All three are optional
   ;; and positional.
   ["-o" "--orderid ORDERID" "Order id"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-u" "--username Username" "User-name"]
   ["-p" "--password Password" "Password"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Essential order scraper."
        "Usage: program-name [options] output-file"
        ""
        "Options:"
        options-summary
        ""
        "Argument: output-file to contain order in tab-separated format"]
       (string/join \newline)
       (println)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn delimit [delimiter x y] (str x delimiter y))

(defn tab-sep-line [row]
  (str (reduce #(delimit "\t" % %2) row) "\n"))

(defn write-file [out-file rows]
  (spit out-file "" :append false)
  (with-open [wrt (io/writer out-file)]
    (doseq [row rows] (.write wrt (tab-sep-line row)))))

(defn save-basket-to-file
  "Saves the basket rows to a text file in tsv format"
  [file username password]
  (try
    (write-file file (site/scrape-basket username password))
    (catch Exception e
     (println (str "Error: " (.getMessage e) " Stacktrace: " (.getStackTrace e)))
     (.printStackTrace e))))

(defn new-save-to-file
  "Saves the basket or specified orderid to file in tsv format"
  [{:keys [username password orderid]} file]
  (println "File: " file " u:" username " p:" password " o:" orderid)
  (try
    (save-basket-to-file file username password)
    (catch Exception e
     (println (str "Error: " (.getMessage e) " Stacktrace: " (.getStackTrace e)))
     (.printStackTrace e))))

(defn -main
  "The application's main function"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
     (:help options) (usage summary)
     (not= (count arguments) 1) (usage summary)
     errors (error-msg errors)
     :else (new-save-to-file options (first arguments)))))
