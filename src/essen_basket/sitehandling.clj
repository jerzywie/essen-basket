(ns essen-basket.sitehandling
  (:require [essen-basket.config       :as data]
            [essen-basket.scrape        :as scrape]
            [net.cgrand.enlive-html :as html]
            [clj-http.client        :as client]))


(def login-url (:login-url data/config-data))
(def basket-url (:basket-url data/config-data))
(def archive-basket-url (:archive-basket-url data/config-data))


(defn get-page [url]
  "Downloads a document as an html-string."
  (let [resp (client/get url)]
    (if (= 200 (:status resp)) (:body resp) nil)))

(defn extract-login-page-input-fields
  "Extracts all form input elements."
  [html-str]
  (html/select (html/html-snippet html-str) [:input]))

(defn get-post-keys-values
  "Extracts form-post keys and values."
  [input-elements]
  (zipmap
   (map #(-> % :attrs :name) input-elements)
   (map #(-> % :attrs :value) input-elements)))

(defn- sub-with
  "Helper function to replace values in map."
  [a b] b)

(defn insert-u-and-p
  "Insert the username and password"
  [u p form-vars-coll]
  (update-in
   (update-in form-vars-coll ["ctl00$MainContent$txtUser"] sub-with u)
    ["ctl00$MainContent$txtPassword"] sub-with p))

(defn remove-unwanted-keys
  "Remove the unwanted key"
  [c] (remove #(= "ctl00$MainContent$cmdForgot" (key %)) c))


;;This extracts the form variables
(defn get-login-vars
  "Extracts form login variables"
  [username password]
  (->> login-url
       get-page
       extract-login-page-input-fields
       get-post-keys-values
       (insert-u-and-p username password)
       remove-unwanted-keys))

;; this gets the login-response
(defn perform-login
  "Do the login and capture the session cookies.
   This only needs to be called for the side-effect
   capturing the session cookies."
  [form-vars]
  (let [cs (clj-http.cookies/cookie-store)
        resp (client/post login-url {:form-params form-vars :cookie-store cs})]
    (if (nil? (get-in resp [:cookies "ETCExtranet"]))
      (throw (ex-info "Login Failed." {:msg "Login Failed."}))
      cs)))

;; get the basket (page 1)
(defn get-basket-page1 [url cookie-store]
  (println "url in use: " url)
  (client/get url {:cookie-store cookie-store}))

(defn basket-next? [html-str]
  "True if there is a next-page link in this HTML."
  (not (nil? (re-find #"Page\$Next" html-str))))


;; The select that retrieves the relevant form variables
(defn extract-basket-page-input-fields
"Extract form vars for next page request"
[html-str]
(html/select (html/html-snippet html-str)
             [#{:input#__VIEWSTATE
                :input#__VIEWSTATEENCRYPTED
                :input#__EVENTVALIDATION}]))


(defn add-next-page-vars
  "Add in post variables to select next page"
  [form-post-vars]
  (into form-post-vars
        {"__EVENTTARGET" "ctl00$MainContent$uxBasket$BasketView",
         "__EVENTARGUMENT" "Page$Next"}))

(defn get-basket-next-page-vars
  "Sets up form-post collection for requesting next basket page.
   Returns nil if no next page."
  [basket-html]
  (if (basket-next? basket-html)
    (->> basket-html
         extract-basket-page-input-fields
         get-post-keys-values
         add-next-page-vars)
    nil))

(defn get-basket-next-page
  "Request next basket page."
  [basket-next-page-vars url cookie-store]
  (client/post url {:form-params basket-next-page-vars
                           :cookie-store cookie-store
                           :save-request? true
                           :debug-body true}))

(defn scrape-data
  "Scrape data from the given url (assumes login is done)."
  [url cookie-store]
  (println "getting page 1")
  (let [page1 (get-basket-page1 url cookie-store)]
    (println "looping over next pages")
    (loop [order-rows (scrape/scrape-order (:body page1))
           next-page-vars (get-basket-next-page-vars (:body page1))]
      (if (nil? next-page-vars)
        (sort order-rows)
        (let  [resp (get-basket-next-page next-page-vars url cookie-store)]
          (recur (into order-rows (scrape/scrape-order (:body resp)))
                 (get-basket-next-page-vars (:body resp))))))))

(defn scrape-archive-order
  "Scrapes archived order off Essential site."
  [orderid username password]

  (let [cookie-store (perform-login (get-login-vars username password))
        url (str archive-basket-url orderid)]
    (scrape-data url cookie-store)))

(defn scrape-basket
  "Scrapes basket off Essential site"
  [username password]

  (let [cookie-store (perform-login (get-login-vars username password))]
    (scrape-data basket-url cookie-store)))
