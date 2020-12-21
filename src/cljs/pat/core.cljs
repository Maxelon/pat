(ns pat.core
  (:require [pat.views.main :as main]
            [pat.utilities :as u]
            [pat.exception :as e]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

;; ASSETS

(rf/reg-event-fx
  :fetch-assets
  (fn [{:keys [db]} _]
    {:db         (assoc db :waiting true)
     :http-xhrio {:method          :get
                  :uri             "/api/assets"
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:load-assets]
                  :on-failure      [:bad-http-result]}}))

(rf/reg-event-db
  :load-assets
  (fn [db [_ response]]
    (let [assets (:assets response)
          assets-map (reduce #(assoc %1 (:aid %2) %2) {} assets)]
      (assoc db :assets assets-map :waiting false))))

(rf/reg-sub
  :get-assets
  (fn [db _]
    (vals (:assets db))))

(rf/reg-sub
  :current-assets
  (fn [_ _]
    (let [assets @(rf/subscribe [:get-assets])
          today @(rf/subscribe [:today])]
      (u/tag-sort (u/current assets today)))))

(rf/reg-sub
  :expired-assets
  (fn [_ _]
    (let [assets @(rf/subscribe [:get-assets])
          today @(rf/subscribe [:today])]
      (u/tag-sort (u/expired assets today)))))

(rf/reg-sub
  :review-assets
  (fn [_ _]
    (let [assets @(rf/subscribe [:get-assets])
          today @(rf/subscribe [:today])]
      (e/failed (u/inspection-sort (u/current assets today))))))

(rf/reg-sub
  :overdue-assets
  (fn [_ _]
    (let [assets @(rf/subscribe [:get-assets])
          today @(rf/subscribe [:today])]
      (e/overdue (u/inspection-sort (u/current assets today)) today))))

(rf/reg-sub
  :search-assets
  (fn [_ _]
    (let [assets @(rf/subscribe [:get-assets])
          search @(rf/subscribe [:get-search])]
      (u/search (u/tag-sort assets) search))))

(defn has-bids [bids result]
  (some #(= (:bid result) %) bids))

(defn contains-bids [bids asset]
  (some #(has-bids bids %) (:results asset)))

(rf/reg-sub
  :assets-from-bids
  (fn [db [_ bids]]
    (let [assets @(rf/subscribe [:get-assets])]
      (filter #(contains-bids bids %) assets))))


;; TEMP-ASSET

(rf/reg-event-db
  :edit-asset
  (fn [db [_ aid]]
    (assoc db :temp-asset ((:assets db) aid))))

(rf/reg-event-db
  :close-edit
  (fn [db [_ _]]
    (-> db (assoc :temp-asset nil))))

(rf/reg-event-db
  :set-result
  (fn [db [_ result]]
    (let [results (get-in db [:temp-asset :results])
          new-results (map #(if (= (:rid result) (:rid %)) result %) results)]
      (assoc-in db [:temp-asset :results] new-results))))

(rf/reg-event-db
  :set-asset
  (fn [db [_ asset]]
    (-> db (assoc :temp-asset asset))))

(rf/reg-event-fx
  :update-asset
  (fn [{:keys [db]} [_ asset]]
    (let [results (get-in db [:temp-asset :results])
          new-results (filter :aid results)
          new-asset (assoc asset :results new-results)]
      {:db         (if (empty? new-results)
                     (assoc db :assets (dissoc (:assets db) (:aid asset)) :temp-asset nil)
                     (assoc db :assets (assoc (:assets db) (:aid asset) new-asset) :temp-asset nil))
       :http-xhrio {:method          :post
                    :uri             "/api/asset"
                    :params          asset
                    :timeout         5000
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [::success-post-result]
                    :on-failure      [::failure-post-result]}})))

(rf/reg-sub
  :edit
  (fn [db _]
    (:temp-asset db)))


(rf/reg-sub
  :get-asset
  (fn [db _]
    (:temp-asset db)))


;; IMPORTS

(rf/reg-event-fx
  :fetch-imports
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             "/api/imports"
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:load-imports]
                  :on-failure      [:bad-http-result]}}))

(rf/reg-event-db
  :load-imports
  (fn [db [_ response]]
    (let [imports (:imports response)
          batches (:batches imports)
          results (:results imports)
          batches-map (reduce #(assoc %1 (:file %2) %2) {} batches)
          results-map (reduce #(assoc %1 (:rid %2) %2) {} results)
          imports-map {:batches batches-map :results results-map}]
      (assoc db :imports imports-map))))

(rf/reg-event-db
  :set-batch
  (fn [db [_ batch]]
    (assoc-in db [:imports :batches (:file batch)] batch)))

(rf/reg-event-fx
  :update-batch
  (fn [{:keys [db]} [_ batch]]
    (let [imports (:imports db)
          batches (dissoc (:batches imports) (:file batch))
          upd-imports (assoc imports :batches batches)]
      {:db         (assoc db :imports upd-imports)
       :http-xhrio {:method          :post
                    :uri             "/api/import"
                    :params          batch
                    :timeout         5000
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:load-imports]
                    :on-failure      [::failure-post-result]}})))


(rf/reg-event-fx
  :link
  (fn [{:keys [db]} [_ result]]
    (let [imports (:imports db)
          results (dissoc (:results imports) (:rid result))
          upd-imports (assoc imports :results results)]
      {:db         (assoc db :imports upd-imports)
       :http-xhrio {:method          :post
                    :uri             "/api/link"
                    :params          result
                    :timeout         5000
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [::success-post-result]
                    :on-failure      [::failure-post-result]}})))

(rf/reg-sub
  :get-batches
  (fn [db _]
    (vals (:batches (:imports db)))))

(rf/reg-sub
  :get-results
  (fn [db _]
    (vals (:results (:imports db)))))


;; REFERENCE DATA

(rf/reg-event-fx
  :fetch-reference
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             "/api/reference"
                  :timeout         8000
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:load-reference]
                  :on-failure      [:bad-http-result]}}))

(rf/reg-event-db
  :load-reference
  (fn [db [_ response]]
    (let [batches (get-in response [:reference :batches])
          locations (get-in response [:reference :locations])
          schedule (get-in response [:reference :schedule])
          reference (assoc (:reference db)
                      :locations locations
                      :batches batches
                      :schedule schedule)]
      (assoc db :reference reference))))

(rf/reg-sub
  :get-locations
  (fn [db _]
    (:locations (:reference db))))

(rf/reg-sub
  :get-last-batches
  (fn [db [_ lid]]
    (let [batches (reverse (sort-by :isodate (get-in db [:reference :batches])))
          sig-batches (filter #(> (:count %) 4) batches)
          search-range (take 12 (drop-while #(not (= lid (:lid %))) sig-batches))]
      (map :bid (filter #(= lid (:lid %)) search-range)))))

(rf/reg-sub
  :asset-count
  (fn [db [_ lid]]
    (let [batches (reverse (sort-by :isodate (get-in db [:reference :batches])))
          sig-batches (filter #(> (:count %) 4) batches)
          search-range (take 12 (drop-while #(not (= lid (:lid %))) sig-batches))]
      (reduce + (map :count (filter #(= lid (:lid %)) search-range))))))

(rf/reg-sub
  :today
  (fn [db _]
    (get-in db [:reference :today])))

(rf/reg-sub
  :schedule
  (fn [db [_ year]]
    (let [schedule (get-in db [:reference :schedule])]
      (:schedule (first (filter #(= (:year %) year) schedule))))))

(rf/reg-sub
  :location
  (fn [db [_ bid]]
    (let [batches (get-in db [:reference :batches])
          batch (first (filter #(= bid (:bid %)) batches))]
      (:lid batch))))


(rf/reg-sub
  :lid-description
  (fn [db [_ lid]]
    (let [locations (get-in db [:reference :locations])
          location (first (filter #(= lid (:lid %)) locations))]
      (:description location))))

;; VIEW & MISC

(rf/reg-event-db
  ::success-post-result
  (fn [db [_ result]]
    (assoc db ::success-post-result result)))

(rf/reg-event-db
  ::failure-post-result
  (fn [db [_ result]]
    (assoc db ::failure-post-result result)))

(rf/reg-event-db
  :bad-http-result
  (fn [db [_ result]]
    (assoc db :bad-http-result result)))

(rf/reg-event-db
  :set-search
  (fn [db [_ search-string]]
    (-> db (assoc :search search-string))))

(rf/reg-event-db
  :set-view
  (fn [db [_ view]]
    (-> db (assoc :view-mode view))))

(rf/reg-sub
  :waiting
  (fn [db _]
    (:waiting db)))

(rf/reg-sub
  :get-search
  (fn [db _]
    (:search db)))

(rf/reg-sub
  :get-view
  (fn [db _]
    (:view-mode db)))


;; INITIALISATION AND SETUP

(rf/reg-event-db
  :initialize
  (fn [_ [_ date]]
    {:assets     {}
     :reference  {:batches [] :locations [] :schedule [] :today date}
     :temp-asset nil
     :imports    {:batches {} :results {}}
     :search     ""
     :waiting    true
     :view-mode  :asset}))

(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (.log js/console "Mounting components...")
  (dom/render [#'main/app] (.getElementById js/document "content"))
  (.log js/console "Components mounted"))

(defn init! []
  (let [today (subs (.toISOString (js/Date.)) 0 10)]
    (.log js/console "Initializing app...")
    (rf/dispatch [:initialize today])
    (rf/dispatch [:fetch-assets])
    (rf/dispatch [:fetch-reference])
    (rf/dispatch [:fetch-imports])
    (mount-components)))