(ns pat.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::rid number?)
(s/def ::aid number?)
(s/def ::bid number?)
(s/def ::lid string?)

(s/def ::tag string?)
(s/def ::description string?)
(s/def ::isodate string?)
(s/def ::type string?)
(s/def ::status string?)
(s/def ::image string?)
(s/def ::earth string?)
(s/def ::insulation string?)
(s/def ::leakage string?)
(s/def ::wiring string?)
(s/def ::inspector string?)
(s/def ::comment string?)
(s/def ::assessment string?)
(s/def ::reason string?)

(s/def ::period number?)

(s/def ::file string?)
(s/def ::count number?)

(s/def ::result
  (s/keys :req-un [::rid
                   ::tag
                   ::description
                   ::isodate
                   ::type
                   ::status
                   ::image
                   ::earth
                   ::insulation
                   ::leakage
                   ::wiring
                   ::inspector
                   ::comment
                   ::bid]
          :opt [::aid
                ::assessment
                ::reason]))

(s/def ::results (s/coll-of ::result))

(s/def ::asset
  (s/keys :req-un [::aid
                   ::description
                   ::results]))

(s/def ::location
  (s/keys :req-un [::lid
                   ::description]
          :opt-un [::period]))

(s/def ::batch
  (s/keys :req-un [::file
                   ::lid
                   ::bid
                   ::isodate
                   ::count]))
