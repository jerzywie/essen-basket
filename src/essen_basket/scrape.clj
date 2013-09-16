(ns essen-basket.scrape
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.set            :as set])
  (:require [clojure.java.io        :as io])
  (:require [clojure.string         :as string])
  (:require [essen-basket.config       :as data]))


(defn leadzeros [[k v]] [(format "%03d" (Integer/valueOf (name k))) v])

;; transform members map in properties into map of members index
(def members (into {} (map leadzeros (:members data/config-data))))


(def selector-odd-row-cells #{[:tr.gridrow :span] [:tr.gridrow :a]})
(def selector-even-row-cells #{[:tr.gridalternatingrow :span] [:tr.gridalternatingrow :a]})

(def cell-selector (set/union selector-odd-row-cells selector-even-row-cells))


(defn get-order-table [source-html]
  (html/select (html/html-snippet source-html) cell-selector))

(defn stringcell [x] (string/trim (first (:content x))))

(defn codecell [x] (-> x :content first :content first))

(defn numbers-only [val] (re-find #"[0-9.]+" val))

(defn getrow [[x1 member code desc pack price vatcode x2 qty cost costvat]]
  (vector
     (stringcell member)
     (get members (stringcell member) "none")
     (codecell code)
     (stringcell desc)
     (stringcell pack)
     (numbers-only (stringcell  price))
     (stringcell vatcode)
     (numbers-only (stringcell qty))
     (numbers-only (stringcell cost))
     (numbers-only (stringcell costvat))))

(defn scrape-order [source-html]
  (for [x (partition 11 (get-order-table source-html))]
     (getrow x)))

(defn delimit [delimiter x y] (str x delimiter y))

(defn tab-sep-line [row]
  (str (reduce #(delimit "\t" % %2) row) "\n"))

(defn write-file [out-file rows]
  (spit out-file "" :append false)
  (with-open [wrt (io/writer out-file)]
    (doseq [row rows] (.write wrt (tab-sep-line row)))))
