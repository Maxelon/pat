(ns pat.views.about
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]))

(defn about-view []
  [:div.content
   [:h1.title.is-2 "About"]
   [:h2.title-is-4 "This app"]
   [:p "This application manages electrical appliances for Immanuel London.
   Test results are captured through the Seaward PatMobile app. Appliances
   are treated as a linked set of results, which this application manages.
   This section describes the policies being followed and the definition of
   some terms as used by Immanuel London."]
   [:h2.title.is-4 "Current Policies"]
   [:p "Note: For a description of some of these terms, see the end of this report"]
   [:h3.subtitle.is-6 "Inspection Policy" ]
   [:p "Assets will be inspected once per year, unless they are in an area
      of restricted access (such as private offices and locked store rooms).
      Restricted areas will be inspected every two years."]
   [:h3.subtitle.is-6 "Test Policy"]
   [:p "Fixed, stationary and IT class 1 assets and
      extension leads will be tested every two years.
      All other class 1 assets and extension leads
      will be tested at every inspection.
      Class 2 assets will be tested every four years.
      Standard power leads will be tested as per the
      asset to which they are attached."]
   [:h3.subtitle.is-6 "Recording Policy"]
   [:p "Details of all assets and extension leads will be held in a central register.
      Standard power leads will not be recorded."]
   [:h3.subtitle.is-6 "Labelling Policy"]
   [:p "PAT testing labels will be placed on assets and leads to indicate
      the last electrical test."]
   [:h2.title-is-6 "Glossary"]
   [:dl
    [:dt "Fixed Equipment"]
    [:dd "Equipment that is reduced risk because it is prevented from moving by some fixture.
        In the case of an extension lead, this may be the presence
        of trunking or mounting screws."]
    [:dt "Stationary Equipment"]
    [:dd "Equipment that is reduced risk because it is not intended to be moved,
        typically because it is heavy and has no wheels, for example,
        a fridge or dishwasher."]
    [:dt "IT Equipment"]
    [:dd "Equipment that is deemed to be a reduced risk. IT equipment is primarily
        low voltage electronics, other than its transformer."]
    [:dt "Other Equipment"]
    [:dd "The IET Code of Practices also recognises hand-held, portable and movable
        equipment. These all represent a higher risk. At Immanuel, they will not
        be further identified."]
    [:dt "Standard Power Leads"]
    [:dd "For the purposes of PAT testing a standard power lead is defined as
        a detachable mains lead of standard design and not more than 2m in
        length, e.g. an IEC lead"]]
   ])


