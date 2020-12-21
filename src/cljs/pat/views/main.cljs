(ns pat.views.main
  (:require [pat.views.assets :as assets]
            [pat.views.about :as about]
            [pat.views.import :as import]
            [pat.views.reports :as reports]
            [pat.asset :as a]
            [pat.utilities :as u]
            [pat.views.edit :as edit]
            [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]))

(defn navbar []
  (let [burger-active (r/atom false)]
    (fn []
      [:nav.navbar.is-info
       [:div.container
        [:div.navbar-brand
         [:a.navbar-item
          {:on-click #(rf/dispatch [:set-view :asset])
           :style    {:font-weight "bold"}}
          "PAT"]
         [:span.navbar-burger.burger
          {:data-target "nav-menu"
           :on-click    #(swap! burger-active not)
           :class       (when @burger-active "is-active")}
          [:span]
          [:span]
          [:span]]]
        [:div#nav-menu.navbar-menu
         {:class (when @burger-active "is-active")}
         [:div.navbar-start
          [:a.navbar-item
           {:on-click #(rf/dispatch [:set-view :import])}
           "Import"]
          [:a.navbar-item
           {:on-click #(rf/dispatch [:set-view :register])}
           "Asset Register"]
          [:a.navbar-item
           {:on-click #(rf/dispatch [:set-view :exceptions])}
           "Exceptions Report"]
          [:a.navbar-item
           {:on-click #(rf/dispatch [:set-view :schedule])}
           "Schedule"]
          [:a.navbar-item
           {:on-click #(rf/dispatch [:set-view :about])}
           "About"]]]]])))

(defn unexpected []
  [:p "Unexpected mode"])

(defn app []
  [:div.app
   [navbar]
   [:section.section
    [:div.container
     (case @(rf/subscribe [:get-view])
       :asset [assets/asset-view]
       :import [import/import-view]
       :register [reports/assets-report]
       :exceptions [reports/exceptions-report]
       :schedule [reports/schedule-report]
       :about [about/about-view]
       [unexpected])
     ]]])
