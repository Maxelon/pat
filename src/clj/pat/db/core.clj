(ns pat.db.core
  (:require
    [next.jdbc.date-time]
    [next.jdbc.result-set]
    [conman.core :as conman]
    [mount.core :refer [defstate]]
    [pat.config :refer [env]]))

(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(extend-protocol next.jdbc.result-set/ReadableColumn
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (.toLocalDateTime v))
  (read-column-by-index [^java.sql.Timestamp v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (read-column-by-label [^java.sql.Date v _]
    (.toLocalDate v))
  (read-column-by-index [^java.sql.Date v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (read-column-by-label [^java.sql.Time v _]
    (.toLocalTime v))
  (read-column-by-index [^java.sql.Time v _2 _3]
    (.toLocalTime v)))

;;Helpers

(defn filled-result
  [result]
  (merge
    {:image      nil
     :earth      nil
     :insulation nil
     :leakage    nil
     :wiring     nil
     :comment    nil}
    result))

(defn raw->id
  "Takes raw SQLITE response (e.g. ({  })) and extracts id"
  [raw]
  (first (vals (first raw))))

(defn result->asset
  [result]
  {:description (:description result)
   :state       "Current"})

(defn match?
  "Takes two results and returns the rid of mb if every tested k-v pair in ma matches mb.
  ma or mb may have additional k-v pairs, but these will not impact the match."
  [ma mb]
  (let [test-keys [:tag :description :type :status :isodate :image :earth
                   :insulation :leakage :wiring :comment :inspector]]
    (if (every? identity (map #(= (% ma) (% mb)) test-keys))
      (:rid mb))))

;;Wrappers

(defn set-asset!
  "wrapper to update or insert an asset"
  [asset]
  (if (:aid asset)
    (if (_asset-exists? asset) (_update-asset! asset))
    (_insert-asset! asset)))

(defn duplicate?
  "Returns a rid if result already in database, else nil"
  [result]
  (let [matched (_matches result)]
    (if (empty? matched)
      nil
      (some identity (map #(match? result %) matched)))))

(defn add-bid!
  "Updates result with the :bid, unless there is already one there"
  [result]
  (let [db-result (get-result result)]
    (when-not (:bid db-result) (_add-bid! result))))

(defn add-result!
  "Inserts a result. It is assumed the lid is present, but asset link missing.
  Duplicate results are quietly ignored.
  Returns result-id (rid) or nil."
  ([result]
   (if (not (duplicate? result))
     (raw->id (_insert-result! (filled-result result)))))
  ([t-conn result options]
   (if (not (duplicate? result))
     (raw->id (_insert-result! t-conn (filled-result result) options)))))

(defn insert-batch!
  "inserts a batch record and returns the id"
  [batch]
  (raw->id (_insert-batch! batch)))

(defn add-asset!
  "Insert a new asset from a result, and link it to that result."
  [result]
  (let [asset (result->asset result)
        rowid (raw->id (_insert-asset! asset))]
    (update-link! (assoc result :aid rowid))
    rowid))

(defn get-lids
  "Returns a vector of lids from all results matching the tag."
  [tag-map]
  (mapv #(:lid %) (_get-lids tag-map)))

(defn get-assets
  "Returns assets with sorted results. If a map with a :tag is provided, then it returns
  all assets linked to the tag. (The tag functionality is currently deprecated."
  ([]
   (let [stubs (_get-stubs)]
     (map #(assoc % :results (reverse (sort-by :isodate (get-results %)))) stubs)))
  ([tag-map]
   (let [stubs (_get-stubs-from-tag tag-map)]
     (map #(assoc % :results (reverse (sort-by :isodate (get-results %)))) stubs))))

(defn envir
  "Returns the environment variable, assumed a key"
  [env-var]
  (:val (_environment {:ref (str env-var)})))

(defn update-batch-count!
  "updates a batch's count based on actual results"
  [bid]
  (_upd-batch-count! {:bid bid}))
