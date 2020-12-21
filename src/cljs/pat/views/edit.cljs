(ns pat.views.edit
  (:require [pat.asset :as a]
            [pat.utilities :as u]
            [pat.compatibility :as c]
            [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]))

(defn result-detail [r first]
  [:div
   (if (not (:aid r))
     {:class "has-text-grey-light"})
   [:div
    [:strong
     (if (not (:aid r))
       {:class "has-text-grey-light"})
     (:tag r) ": " (:description r)]
    [:small " " (:status r)]]
   [:div
    [:small (:isodate r) "/" (:lid r) " Test type: " (:type r)]]
   (if (:insulation r)
     [:div
      [:small
       (str (if (:earth r) (str "Earth: " (:earth r) "; "))
            (if (:insulation r) (str "Insulation: " (:insulation r) "; "))
            (if (:leakage r) (str "Leakage: " (:leakage r) "; "))
            (if (:wiring r) (str "Wiring: " (:wiring r) ".")))]]
     [:div
      [:small " No recorded test"]])
   [:div
    [:small (:inspector r) (if (:comment r) (str " - " (:comment r)))]]
   (if first
     [:div.field.has-addons
      [:div.select
       [:select
        (merge {:value     (if (:assessment r) (:assessment r) "-")
         :on-change #(rf/dispatch [:set-result (assoc r :assessment (-> % .-target .-value))])}
               (if (:aid r) {} {:disabled true}))
        [:option "-"]
        [:option {:value "PASS"} "Pass"]
        [:option {:value "FAIL"} "Fail"]]]
      [:input.input
       (merge {:type :text
        :on-change #(rf/dispatch [:set-result (assoc r :reason (-> % .-target .-value))])
        :value (:reason r)} (if (:aid r) {} {:disabled true}))]]
     (if (:assessment r)
       [:div.small (:assessment r) (:reason r)]))])

(defn result-component [r first]
  [:article.media
   [:figure.media-left
    (if (:image r)
      [:p.image.is-64x64
       [:img {:src (str "/images/" (:image r))}]])]
   [:div.media-content
    [:div.content
     [result-detail r first]]]
   [:div.media-right
    [:div [:button.button.is-small
           (merge {:on-click #(rf/dispatch [:set-result (assoc r :aid nil)])}
                  (if (:aid r) {} {:disabled true}))
           "remove"]]]])

(defn asset-modal []
  (let [asset @(rf/subscribe [:get-asset])]
    (fn []
      [:div.modal.is-active
       [:div.modal-background
        {:on-click #(rf/dispatch [:close-edit])}]
       [:div.modal-card
        [:header.modal-card-head
         [:div.modal-card-title
          [:div.control
           [:div.modal-card-title
            [:div.field
             [:input.input
              {:type      "text"
               :value     (:description @(rf/subscribe [:get-asset]))
               :on-change #(rf/dispatch [:set-asset (assoc asset :description (-> % .-target .-value))])}]
             ]
            ]]]]
        [:section.modal-card-body
         (doall (for [r (:results @(rf/subscribe [:get-asset]))]
           ^{:key (:rid r)} [result-component r (= r (first (:results @(rf/subscribe [:get-asset]))))]))]
        [:footer.modal-card-foot
         [:div
          "This "
          (a/env-type asset) " class " (a/asset-class asset) " appliance is "
          (if (u/expired? asset (c/today)) "not currently managed." "currently managed. ")]
         [:button.button.is-success
          {:on-click #(rf/dispatch [:update-asset @(rf/subscribe [:get-asset])])}
          "Save changes"]
         [:button.button
          {:on-click #(rf/dispatch [:close-edit])}
          "Cancel"]]]])))