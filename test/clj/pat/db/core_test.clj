(ns pat.db.core-test
  (:require
    [pat.db.core :refer [*db*] :as db]
    [java-time.pre-java8]
    [luminus-migrations.core :as migrations]
    [clojure.test :refer :all]
    [next.jdbc :as jdbc]
    [pat.config :refer [env]]
    [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'pat.config/env
      #'pat.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-result
  (jdbc/with-transaction
    [t-conn *db* {:rollback-only true}]
    (is (not (nil?
               (def r-id (db/add-result!
                           t-conn
                           {:tag         "001"
                            :description "An asset"
                            :type        "EARTH"
                            :status      "PASS"
                            :isodate     "2018-01-01"
                            :inspector   "Ken"
                            :lid         "A1"}
                           {})))))
    (is (= {:tag         "001"
            :description "An asset"
            :type        "EARTH"
            :isodate     "2018-01-01"
            :image       nil
            :earth       nil
            :lid         "A1"}
           (select-keys
             (db/get-result
               t-conn
               {:rid r-id}
               {})
             [:tag :description :type :isodate :image :earth :lid])))))

(deftest test-result-duplicate
  (do
    (db/add-result!
      {:tag         "999"
       :description "An asset"
       :type        "EARTH"
       :status      "PASS"
       :isodate     "2018-01-01"
       :inspector   "Ken"
       :lid "A1"})
    (is (nil?
          (db/add-result!
            {:tag         "999"
             :description "An asset"
             :type        "EARTH"
             :status      "PASS"
             :isodate     "2018-01-01"
             :inspector   "Ken"
             :lid "A1"})))))