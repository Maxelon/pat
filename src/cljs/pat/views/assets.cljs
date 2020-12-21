(ns pat.views.assets
  (:require [pat.asset :as a]
            [pat.utilities :as u]
            [pat.views.edit :as edit]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn date-detail [asset]
  [:dd.is-size-7 {:key "date"}
   (str "Last inspection: " (:isodate (a/last-inspection asset))
        "/" @(rf/subscribe [:location (:bid (a/last-inspection asset))]))
   (if (a/last-test asset)
     (str " - Last test: " (:isodate (a/last-test asset)))
     " - No recorded test")])

(defn expired-detail [asset]
  [:dd.is-size-7 {:key "expired"} (:expired asset)])

(defn result-detail [asset]
  (let [last-test (a/last-test asset)]
    (if last-test
      [:dd.is-size-7 {:key "result"}
       (str (:isodate last-test) "/"
            @(rf/subscribe [:location (:bid last-test)]) ": "
            (:status last-test) ": "
            (if (:earth last-test) (str "Earth: " (:earth last-test) "; "))
            (if (:insulation last-test) (str "Insulation: " (:insulation last-test) "; "))
            (if (:leakage last-test) (str "Leakage: " (:leakage last-test) "; "))
            (if (:wiring last-test) (str "Wiring: " (:wiring last-test) ".")))]
      [:dd.is-size-7 "No recorded test"])))

(defn review-detail [asset]
  [:dd.is-size-7 {:key "review"} (:failure asset)])

(defn issue-detail [asset]
  [:dd.is-size-7 {:key "issue"} (str "Issue: " (:overdue asset))])

(defn tag-detail [asset]
  [:dd.is-size-7 {:key "tag"} (str "All tags: " (map :tag (:results asset)))])

(defn asset-list [assets details]
  [:div (if @(rf/subscribe [:waiting])
          [:p "Loading data..."]
          [:p (count assets) " items"])
   (doall
     (for [asset assets]
       ^{:key (:aid asset)}
       [:div.level.is-mobile #_{:key (:aid asset)}
        [(if (:expired asset)
           :div.level-left>div.level-item.has-text-grey-light
           :div.level-left>div.level-item)
         [:dl
          [:dt
           (str (a/last-tag asset) ": "
                (:description asset)
                " (" (:aid asset) ")")]
          (for [detail details] (detail asset))]]
        [:div.level-right
         [:div.level-item
          [:button.button.is-small.is-light.is-rounded.is-info
           {:on-click #(rf/dispatch [:edit-asset (:aid asset)])}
           "info"]]]]))])

(defn search []
  (let [srch-txt (r/atom "")
        current (r/atom false)]
    (fn []
      [:div
       [:div.field.is-horizontal.is-grouped
        [:div.field.has-addons
         [:p.control>input.input.is-small.is-rounded
          {:type        :text
           :placeholder "Enter tag or description"
           :value       @srch-txt
           :on-change   #(reset! srch-txt (-> % .-target .-value))}]
         [:p.control>button.button.is-small.is-rounded
          {:on-click #(reset! srch-txt "")}
          "clear"]]
        [:label.checkbox
         [:input.ml-4.mr-2
          {:type      "checkbox"
           :value     "current"
           :checked   @current
           :on-change #(swap! current not)}] "Show only current"]]
       (let [all-assets @(rf/subscribe [:get-assets])
             today @(rf/subscribe [:today])
             assets (if @current
                      (u/current all-assets today)
                      (u/add-expired all-assets today))]
         [asset-list (u/search assets @srch-txt) [tag-detail date-detail expired-detail]])
       ])))

(defn asset-view []
  (let [view (r/atom :assets)]
    (fn []
      [:div.content
       (if @(rf/subscribe [:edit]) [edit/asset-modal])
       [:div.tabs.is-centered>ul
        [:li {:class (if (= :assets @view) "is-active")}
         [:a {:on-click #(reset! view :assets)} "Assets"]]
        [:li {:class (if (= :review @view) "is-active")}
         [:a {:on-click #(reset! view :review)} "Review"]]
        [:li {:class (if (= :overdue @view) "is-active")}
         [:a {:on-click #(reset! view :overdue)} "Overdue"]]]
       (case @view
         :assets [search]

         :review [asset-list @(rf/subscribe [:review-assets])
                  [date-detail result-detail review-detail]]
         :overdue [asset-list @(rf/subscribe [:overdue-assets])
                   [date-detail issue-detail]])])))