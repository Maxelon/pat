(ns pat.compatibility
  (:require [java-time :as jt]))

(defn read-str
  "Converts a string to a number"
  [string]
  (read-string string))
