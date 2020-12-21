(ns pat.compatibility
  (:require [cljs.reader :as r]))


(defn read-str
  "Converts a string to a number"
  [string]
  (r/read-string string))

(defn today [] (subs (.toISOString (js/Date.)) 0 10))
