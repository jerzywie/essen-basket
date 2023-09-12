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

(def cells-per-row 9)

(defn get-order-table [source-html]
  (html/select (html/html-snippet source-html) cell-selector))

(defn stringcell [html] (-> html :content first string/trim (string/replace #"\s+" " ")))

(defn codecell [html] (-> html :content first))

(defn numbers-only [val] (re-find #"[0-9.]+" val))

(defn get-vatcode [vatamount] (if (= vatamount "0.00") "Z" "V"))

(defn getrow [[member-h code-h desc-h pack-h price-h _ qty-h cost-h vatcost-h]]
  (let [member      (stringcell member-h)
        member-name (get pad-member member "none")
        code        (codecell code-h)
        desc        (stringcell desc-h)
        pack        (stringcell pack-h)
        price       (numbers-only (stringcell price-h))
        qty         (numbers-only (stringcell qty-h))
        cost        (numbers-only (stringcell cost-h))
        costvat     (numbers-only (stringcell vatcost-h))
        vatcode     (get-vatcode costvat)]
    (vector
     member
     member-name
     code
     desc
     pack
     price
     vatcode
     qty
     cost
     costvat)))

(defn scrape-order [source-html]
  (for [row-html (partition cells-per-row (get-order-table source-html))]
     (getrow row-html)))
