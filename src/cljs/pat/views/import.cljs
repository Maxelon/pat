(ns pat.views.import
  (:require [pat.asset :as a]
            [pat.utilities :as u]
            [pat.views.edit :as edit]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn result-detail [result]
  [:dd.is-size-7
   (:isodate result) " > "
   (:tag result) ": "
   (:description result) ": "
   (:status result) " "
   (if (:earth result) (str "Earth: " (:earth result) "; "))
   (if (:insulation result) (str "Insulation: " (:insulation result) "; "))
   (if (:leakage result) (str "Leakage: " (:leakage result) "; "))
   (if (:wiring result) (str "Wiring: " (:wiring result) "."))])

(defn match [result]
  (let [text (r/atom (:tag result))]
    (fn []
      [:div
       [:div.field.is-horizontal.is-grouped
        [:field-label.is-normal
         [:label.label "Assets matching: "]]
        [:div.field-body
         [:div.field>div.control>input.input.is-small.is-rounded
          {:type        :text
           :placeholder "Enter tag or description"
           :value       @text
           :on-change   #(reset! text (-> % .-target .-value))}]]]
       (let [asset-list (u/search @(rf/subscribe [:get-assets]) @text)]
         (doall
           (for [asset asset-list]
             ^{:key (:aid asset)}
             [:table.table>tbody
              [:tr
               [:td
                [:dl
                 [:dt (:description asset)]
                 (doall
                   (for [r (:results asset)]
                     ^{:key (:rid r)}
                     [result-detail r]))]]
               [:td
                [:button.button.is-small
                 {:on-click #(rf/dispatch [:link (assoc result :aid (:aid asset))])}
                 "Link"]]]])))])))

(defn import-view []
  (let [locations @(rf/subscribe [:get-locations])
        batches @(rf/subscribe [:get-batches])
        results @(rf/subscribe [:get-results])]
    [:div.content
     [:h1 "Import page (tidy later)"]
     [:h2 "Orphan Results"]
     (if (empty? results)
       [:div
        [:p "There are no results"]]
       (doall
         (for [result results]
           ^{:key (:rid result)}
           [:div.media
            [:figure.media-left
             (if (:image result)
               [:p.image.is-64x64
                [:img {:src (str "/images/" (:image result))}]])]
            [:div.media-content
             [:div.content
              [:div
               [:div
               [:strong (:status result) " "]
               [:strong (:tag result) ": "]
               [:strong (:description result) " "]
               [:small (:isodate result) " "]]
              [:div
               (if (:insulation result)
                 (str
                   (if (:earth result) (str "Earth: " (:earth result) "; "))
                   (if (:insulation result) (str "Insulation: " (:insulation result) "; "))
                   (if (:leakage result) (str "Leakage: " (:leakage result) "; "))
                   (if (:wiring result) (str "Wiring: " (:wiring result) ".")))
                 "No recorded test")]
              [:div (:comment result)]]
              [match result]
              [:div.field
               [:button.button.is-small
                {:on-click #(rf/dispatch [:link result])}
                "Create new asset"]]

              ]]])))
     [:h2 "Pending Batches"]
     (if (empty? batches)
       [:div
        [:p "There are no batches"]]
       [:div
        (doall
          (for [batch batches]
            ^{:key (:file batch)}
            [:div.level
             [:div.level-left
              [:div.level-item (:count batch) " results in " (:file batch)]

              ]
             [:div.level-right
              [:div.level-item.select
               [:select
                {:value     (:lid batch)
                 :on-change #(rf/dispatch [:set-batch (assoc batch :lid (-> % .-target .-value))])}
                (doall (for [l locations]
                         ^{:key (:lid l)} [:option {:value (:lid l)} (:description l)]))]]
              [:div.level-item
               [:button.button.is-primary
                {:on-click #(rf/dispatch [:update-batch (assoc batch :accept true)])}
                "Accept"]
               [:button.button.is-danger
                {:on-click #(rf/dispatch [:update-batch (assoc batch :reject true)])}
                "Reject"]]]]))]
       )]))
