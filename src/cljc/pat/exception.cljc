(ns pat.exception
  (:require [pat.utilities :as u]
            [pat.asset :as a]))

(def not-nil? (complement nil?))

(defn policy
  "Returns a policy map for the supplied asset"
  [asset]
  (let [class (a/asset-class asset)
        cl1 {:class 1 :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.75}
        cl2 {:class 2 :insulation-lim 2.0 :leakage-lim 0.25}]
    (if (= class 2)
      cl2
      cl1)))

;; Asset test result handling
(defn status-error?
  [result]
  (if (= "PASS" (:status result))
    nil
    (str "Result status is: " (if (:status result) (:status result) "nil"))))

(defn earth-error?
  [result plcy]
  (let [pass-strings #{"PASS" "<0.01" "< 0.01"}]
    (cond
      (= 2 (:class plcy)) nil
      (nil? (:earth result)) "No earth result."
      (contains? pass-strings (:earth result)) nil
      (not (number? (u/to-num (:earth result))))
        (str "Failed earth result: " (:earth result))
      (<= (u/to-num (:earth result)) (:earth-lim plcy)) nil
      :else (str "High earth value: " (:earth result)))))

(defn insulation-error?
  [result plcy]
  (let [pass-strings #{"PASS" ">19.99" "> 19.99"}]
    (cond
      (nil? (:insulation result)) "No insulation result."
      (contains? pass-strings (:insulation result)) nil
      (not (number? (u/to-num (:insulation result))))
        (str "Failed insulation result: " (:insulation result))
      (>= (u/to-num (:insulation result)) (:insulation-lim plcy)) nil
      :else (str "Low insulation value: " (:insulation result)))))

(defn leakage-error?
  [result plcy]
  (let [pass-strings #{"PASS" "<0.10" "< 0.10"}]
    (cond
      (nil? (:leakage result)) nil
      (contains? pass-strings (:leakage result)) nil
      (not (number? (u/to-num (:leakage result))))
        (str "Failed leakage result: " (:leakage result))
      (<= (u/to-num (:leakage result)) (:leakage-lim plcy)) nil
      :else (str "High leakage value: " (:leakage result)))))

(defn wiring-error?
  [result]
  (if (or (= "GOOD" (:wiring result)) (nil? (:wiring result)))
    nil
    (str "Wiring error: " (:wiring result))))

(defn error?
  "Returns an error message if the result does not comply with plcy,
  or nil if it complies. Checks first if it has been manually assessed,
  if it has then this overrides any automated checking."
  [result plcy]
  (if (nil? (:assessment result))
    (cond
    (status-error? result) (status-error? result)
    (earth-error? result plcy) (earth-error? result plcy)
    (insulation-error? result plcy) (insulation-error? result plcy)
    (leakage-error? result plcy) (leakage-error? result plcy)
    :else (wiring-error? result))
    (if (= "PASS" (:assessment result))
      nil
      (:reason result))))

;; Test schedule issue handling

(defn missed-test?
  "Returns an error message if there is no test, or if it is outside the
  expected test-period."
  [asset]
  (let [test-period (cond
                      (= 2 (a/asset-class asset)) 48
                      (contains? #{:stationary :it :fixed} (a/env-type asset)) 24
                      :else 12)
        inspection-date (:isodate (a/last-inspection asset))
        test-date (if (a/last-test asset)
                    (:isodate (a/last-test asset))
                    nil)]
    (if test-date
      (if (u/before? inspection-date (u/advance test-date test-period) )
        nil
        (str "Test missed at last inspection: " (:isodate (a/last-inspection asset))))
      "No test recorded.")))
        
        
(defn missed-inspection?
  "Returns an error message if the inspection is overdue at the given date.
  Nominally, this is taken as anything over 24 months. This covers the 12
  months (plus 12 months due to asset movement), for most assets, or the 24
  months (plus no allowance for movement), for assets in a secure location.
  1 further month is added to allow for day of month differences.
  Asset input needs to be enriched with reporting data. "
  [asset date]
  (let [last-inspected-date (if (a/last-inspection asset)
                              (:isodate (a/last-inspection asset))
                              "2000-01-01")
        due-date (u/advance last-inspected-date 25)]
    (if (u/before? due-date date)
      "Inspection overdue.")))

(defn overdue?
  "Returns an error message if there is an issue with the schedule,
  or nil if there are no issues."
  [asset date]
  (cond
    (missed-test? asset) (missed-test? asset)
    (missed-inspection? asset date) (missed-inspection? asset date)
    :else nil))

;; Exceptions reporting

(defn assoc-failure
  "Takes an asset and if the last inspection is not a pass, or the last-test does not
   comply with the policy, it adds a failure reason to :failure."
  [asset]
  (let [plcy (policy asset)
        last-test (a/last-test asset)
        last-inspection (a/last-inspection asset)]
    (cond
    (:assessment last-inspection)
      (if (= "PASS" (:assessment last-inspection))
        asset
        (assoc asset :failure (:reason last-inspection)))
    (status-error? last-inspection)
      (assoc asset :failure (status-error? last-inspection))
    (nil? last-test) asset
    (error? last-test plcy)
      (assoc asset :failure (error? last-test plcy))
    :else asset)))

(defn failed
  "Takes a list of assets and returns failed assets, adding a reason in :failure."
  [assets]
  (filter #(:failure %) (map #(assoc-failure %) assets)))
  
(defn assoc-overdue
  "Takes an enriched asset and if there are issues between the PAT tests performed
  and the scheduled expectation, it adds an issue reason to :issue.
  If no date is provided, then defaults to today."
  [asset date]
  (if (overdue? asset date)
    (assoc asset :overdue (overdue? asset date))
    asset))

(defn overdue
  "Takes a list of assets and returns assets that are overdue at the date,
  adding a reason in :issue"
  [assets date]
  (filter #(:overdue %) (map #(assoc-overdue % date) assets)))