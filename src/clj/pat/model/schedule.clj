(ns pat.model.schedule
  (:require
    [pat.db.core :as db]
    [java-time :as jt]))

;; Pure



#_(defn isodate->year
    "Takes a date string in iso format and returns the year as an integer"
    [date-string]
    (read-string (clojure.string/join (take 4 date-string))))


;;impure

#_(defn enrich-event
  "Add assets and location to event. Use previous years assets, or if none,
  use the assets from the year before."
  [event]
  (let [year (- (:year event) 1)
        assets (db/assets-at-location {:lid    (:lid event)
                                       :start  (str year "-01-01")
                                       :finish (str year "-12-31")})]
    (if (empty? assets)
      (assoc event
        :location ((db/locations) (:lid event))
        :assets (db/assets-at-location {:lid    (:lid event)
                                        :start  (str (- year 1) "-01-01")
                                        :finish (str (- year 1) "-12-31")}))
      (assoc event
        :location ((db/locations) (:lid event))
        :assets assets))))
;;pure
(defn monthly
  "Returns a schedule of events for a given month"
  [schedule month-num]
  (sort-by :lid (filter #(= month-num (:month %)) schedule)))

#_(defn months
  "Creates a vector of months and populates with the schedule"
  [schedule]
  (let [events (map #(enrich-event %) schedule)
        month-vec [{:num 1 :name "January"} {:num 2 :name "February"}
                   {:num 3 :name "March"} {:num 4 :name "April"}
                   {:num 5 :name "May"} {:num 6 :name "June"}
                   {:num 7 :name "July"} {:num 8 :name "August"}
                   {:num 9 :name "September"} {:num 10 :name "October"}
                   {:num 11 :name "November"} {:num 12 :name "December"}]]
    (map #(assoc % :soe (monthly events (:num %))) month-vec)))



;; Impure


(defn current-year []
  (let [date (.format (java.text.SimpleDateFormat. "yyyy") (new java.util.Date))]
    (read-string (.toString date))))

(defn next-year []
  (inc (current-year)))

(defn today
  "Returns today as an isostring"
  []
  (str (jt/local-date)))


#_(defn schedule-report
  [year]
  (let [schedule (db/get-schedule {:year year})]
    (assoc {:year (str year)}
      :months (months schedule))))

;; Rework

(defn by-month
  "takes a vector of yearly events and places in a map of months.
  Year is ignored"
  [events]
  (for [month (range 1 13)]
    (assoc {} :month month :events (map :lid (filter #(= month (:month %)) events)))))

(defn schedule
  "Creates a map of the schedule"
  []
  (let [list (db/list-schedule)
        current (filter #(= (current-year) (:year %)) list)
        next (filter #(= (next-year) (:year %)) list)]
    [{:year (current-year)
     :schedule (by-month current)}
     {:year (next-year)
     :schedule (by-month next)}]))