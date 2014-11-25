(ns essen-basket.scrape
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.set            :as set])
  (:require [clojure.string         :as string])
  (:require [essen-basket.config       :as data]))


(defn leadzeros [[k v]] [(format "%03d" (Integer/valueOf (name k))) v])

(defn pad-name-tuple
  [[n {:keys [pad]}]]
  [(format "%03d" (Integer/valueOf pad)) (name n)])

;; transform members map in properties into map of pad - member
(def pad-member (into {} (map pad-name-tuple (:members data/config-data))))

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
     (get pad-member (stringcell member) "none")
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
