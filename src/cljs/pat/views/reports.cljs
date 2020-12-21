(ns pat.views.reports
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [pat.asset :as a]
            [pat.utilities :as u]
            [clojure.string :as s]))

(defn tag-list [a]
  (let [tags (set (map :tag (:results a)))]
    (subs (s/join (for [t tags]
                    (str ", " t))) 1)))

(defn environment [a]
  (case (a/env-type a)
    :it "IT"
    :fixed "Fixed"
    :stationary "Stationary"
    :portable "Portable"))

(defn assets-report []
  (let [assets @(rf/subscribe [:current-assets])]
    [:div.content
     [:h1 "Immanuel Asset Register"]
     [:p "There are " (count assets) " assets currently being managed."]
     [:table.table.is-narrow.is-striped.is-bordered
      [:thead.is-size-7
       [:tr.has-text-left
        [:th "Asset"]
        [:th "Class"]
        [:th "Type"]
        [:th "Inspected"]
        [:th "Tested"]
        [:th "Linked tags"]]]
      [:tbody.is-size-7
       (doall
         (for [a assets]
           ^{:key (:aid a)}
           [:tr
            [:td (a/last-tag a) ": " (:description a)]
            [:td (a/asset-class a)]
            [:td (environment a)]
            [:td (:isodate (a/last-inspection a))]
            [:td (:isodate (a/last-test a))]
            [:td (tag-list a)]]))]]]))

(defn exceptions-report []
  (let [failed @(rf/subscribe [:review-assets])
        overdue @(rf/subscribe [:overdue-assets])
        exceptions (reverse (sort-by #(:isodate (a/last-inspection %)) (concat failed overdue)))]
    [:div.content
     [:h1.title.is-2 "Exceptions Report"]
     [:p "There are " (count exceptions) " exceptions"]
     [:table.table.is-narrow.is-striped.is-bordered
      [:thead.is-size-7
       [:tr.has-text-left
        [:th "Asset"]
        [:th "Inspected"]
        [:th "Location"]
        [:th "Exception"]]]
      [:tbody.is-size-7
       (doall
         (for [a exceptions]
           ^{:key (:aid a)}
           [:tr
            [:td (a/last-tag a) ": " (:description a)]
            [:td (:isodate (a/last-inspection a))]
            [:td @(rf/subscribe [:location (:bid (a/last-inspection a))])]
            [:td (if (:failure a) (:failure a) (:overdue a))]
            ]))]]
     ]))

(defn current-detail [month schedule]
  (let [current-month (first (filter #(= month (:month %)) schedule))
        events (:events current-month)]
    (doall
      (for [e events]
        [:div.mb-4 {:key e}
         [:h4.subtitle.is-6.mb-2 @(rf/subscribe [:lid-description e])]
         (let [bids @(rf/subscribe [:get-last-batches e])
               assets @(rf/subscribe [:assets-from-bids bids])]
           [:div.is-size-7.ml-4 (doall (for [a assets]
                                         (str (:tag (a/last-inspection a)) ":" (:description a) "; ")))])]))))

(defn next-detail [month schedule]
  (let [next-month (first (filter #(= month (:month %)) schedule))
        events (:events next-month)]
    (doall
      (for [e events]
        [:div.mb-4 {:key e}
         [:h4.subtitle.is-6.mb-2 @(rf/subscribe [:lid-description e])]
         (let [count @(rf/subscribe [:asset-count e])]
           [:div.is-size-7.ml-4 (str count " Assets ")])]))))

(defn schedule-report []
  (let [crt-yr (u/year @(rf/subscribe [:today]))
        current @(rf/subscribe [:schedule crt-yr])
        nxt-yr (inc crt-yr)
        next @(rf/subscribe [:schedule nxt-yr])]
    [:div.content
     [:h1.title.is-2 "Schedule Report"]
     [:h2.title.is-3 crt-yr " Schedule"]
     (doall
       (for [month (range 1 13)]
         ^{:key month}
         [:div
          [:h3.title.is-4 (u/month month) " " crt-yr]
          (current-detail month current)]))
     [:h2.title.is-3 nxt-yr " Schedule"]
     (doall
       (for [month (range 1 13)]
         ^{:key month}
         [:div
          [:h3.title.is-4 (u/month month) " " nxt-yr]
          (next-detail month current)]))]))


(defn report-view []
  [:div
   [:ul
    [:li>a
     {:href "/register"}
     "Asset Register"]
    [:li>a
     {:href "/exceptions"}
     "Exceptions"]
    [:li>a
     {:href "/schedule"}
     "Schedule"]]])
