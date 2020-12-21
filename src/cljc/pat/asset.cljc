(ns pat.asset
  (:require [clojure.string :as str]))
;; The asset is largely virtual. A stub (id and description) is held on the db,
;; which will add the results before returning. The rest is derived here.

(defn last-test
  "Returns the last result attached to the asset that has tests."
  [asset]
  (let [results (:results asset)]
    (if (empty? results)
      nil
      (if (some? (:insulation (first results)))
        (first results)
        (last-test (assoc asset :results (rest results)))))))

(defn last-inspection
  "Returns the last result attached to the asset."
  [asset]
  (let [results (:results asset)]
    (if (empty? results)
      nil
      (first results))))

(defn last-tag
  "Returns the last 4-digit tag, or if there isn't one, the last tag."
  [asset]
  (let [results (filter #(re-find #"\d{4}" (:tag %)) (:results asset))]
    (if (empty? results)
      (:tag (first (:results asset)))
      (:tag (first results)))))

(defn asset-class
  "Determines the class from previous test results, or all results 
  if there are no tests."
  [asset]
  (let [all-results (:results asset)
        all-tests (filter :insulation all-results)
        results (if (empty? all-tests) all-results all-tests)
        to-class #(if (= "INSULATION" (:type %)) 2 1)]
    (case (count results)
      0 1
      1 (to-class (first results))
      (if (=  (to-class (first results)) (to-class (second results)))
        (to-class (first results))
        (asset-class (assoc asset :results (rest results)))))))

(defn env-type
  "Determines the environment type from the asset stub (description)"
  [asset]
  (let [words (str/split
                (str/lower-case (:description asset))
                #" ")
        fixed #{"fixed"}
        it #{"it" "dell" "computer" "monitor" "netgear" "pc" "printer" "laptop"}
        stationary #{"stationary" "fridge" "microwave"}]
    (cond
      (some #(fixed %) words) :fixed
      (some #(it %) words) :it
      (some #(stationary %) words) :stationary
      :else :portable)))

(defn assoc-summary
  "Takes an asset and merges in a summary of the results"
  [asset]
  (assoc asset
    :env-type (env-type asset)
    :tag (last-tag asset)
    :tags (mapv :tag (:results asset))
    :class (asset-class asset)
    :last-inspection (last-inspection asset)
    :last-test (last-test asset)))

(defn enrich
  "Takes a sequence of assets and enriches with summary information."
  [assets]
  (map assoc-summary assets))




