(ns pat.utilities
  (:require [pat.compatibility :as c]))


;; General utilities

(defn to-num [str]
  (cond
    (some #(= \. %) str) (c/read-str str)
    :else (if (= \0 (first str))
            (c/read-str (subs str 1))
            (c/read-str str))))
;; ISO date utilities

(defn date
  "Converts an ISO date to a map"
  [isodate]
  (let [year (to-num (subs isodate 0 4))
        month (to-num (subs isodate 5 7))
        day (to-num (subs isodate 8 10))]
    {:year year :month month :day day}))

(defn isodate
  "Converts a date map into an isodate string"
  [date-map]
  (let [{:keys [year month day]} date-map
        to-s #(if (< % 10) (str "0" %) (str %))]
    (str year "-" (to-s month) "-" (to-s day))))

(defn year
  "takes an isodate and returns the year as an integer"
  [isodate]
  year (to-num (subs isodate 0 4)))

(defn month
  "takes a month number and returns the month name"
  [num]
  (let [month [nil "January" "February" "March" "April"
               "May" "June" "July" "August" "September"
               "October" "November" "December"]]
    (month num)))

(defn advance
  "Advance an isodate by a given number of months"
  [start-date months]
  (let [date (date start-date)
        old-year (:year date)
        old-month (:month date)
        new-month (+ old-month months)
        year (+ old-year (quot new-month 12))
        month (rem new-month 12)]
    (isodate (assoc date :year year :month month))))

(defn before?
  "Compares two dates and returns true if the first date is before the second"
  [a b]
  (> 0 (compare a b)))

;; asset filters and sorting, shared between clojure and clojurescript

(defn inspection-sort
  "Takes a sequence of assets and sorts them by inspection date,
  most recent first."
  [assets]
  (reverse (sort-by #(:isodate (first (:results %))) assets)))

(defn tag-sort
  "Takes a sequence of assets and sorts them by the last result :tag"
  [assets]
  (sort-by #(:tag (first (:results %))) assets))


(defn tag?
  "Returns true if the asset contains the specified tag. Ignores case"
  [t asset]
  (some #(= (clojure.string/upper-case t) (clojure.string/upper-case (:tag %))) (:results asset)))

(defn match?
  "Returns true if the asset description contains the specified string. Ignores case."
  [s asset]
  (re-seq (re-pattern (clojure.string/upper-case s)) (clojure.string/upper-case (:description asset))))

(defn search
  "Searches through a list of assets and returns those that match on description or tag"
  [assets s]
  (filter #(or (tag? s %) (match? s %)) assets))

(defn expired?
  "Returns a string giving the reason for expiring the asset,
  or nil if stll current, based on supplied date. Item marked as removed or not
  applicable, by annotating the asset description or the last-inspection."
  [asset date]
  (let [last-inspection (first (:results asset))
        inspected-date (:isodate last-inspection)
        expire-date (advance inspected-date 30)
        status (if (:assessment last-inspection)
                 (:assessment last-inspection)
                 (:status last-inspection))
        description (clojure.string/lower-case
                      (str (:description asset) " "
                           (:description last-inspection)))]
    (cond
      (re-seq #"removed" description) (str "Item removed.")
      (re-seq #"n/a" description) (str "Item not applicable.")
      (not (= status "PASS")) (str "Item failed " inspected-date)
      (before? expire-date date) (str "Item missing. Last seen " inspected-date))))

(defn add-expired
  "Returns all assets, adding a :expired tag and
  reason"
  [assets date]
  (map #(if (expired? % date)
          (assoc % :expired (expired? % date)) %)
       assets))

(defn expired
  "Returns just the expired assets, adding a :expired tag, with
  a reason"
  [assets date]
  (let [assets (add-expired assets date)]
    (filter :expired assets)))

(defn current
  "Returns just the current assets"
  [assets date]
  (let [assets (add-expired assets date)]
    (filter #(not (:expired %)) assets)))


