(ns pat.utilities_test
  (:require [pat.utilities :as u]
            [clojure.test :refer :all]))

(deftest test-expired?
  (testing ":status"
    (is (= nil
           (u/expired? {:results [{:isodate "2021-01-01" :status "PASS"}]}  "2020-01-01")))
    (is (= "Item failed 2021-01-01"
           (u/expired? {:results [{:isodate "2021-01-01" :status "FAIL"}]}  "2020-01-01")))
    (is (= "Item removed."
           (u/expired? {:description "monitor removed"
                        :results [{:isodate "2021-01-01" :status "FAIL"}]}  "2020-01-01")))
    (is (= "Item removed."
           (u/expired? {:results [{:isodate "2021-01-01" :status "FAIL" :description "monitor removed"}]}
                       "2020-01-01")))
    (is (= "Item not applicable."
           (u/expired? {:description "monitor n/a"
                        :results [{:isodate "2021-01-01" :status "FAIL"}]}  "2020-01-01")))
    (is (= "Item not applicable."
           (u/expired? {:results [{:isodate "2021-01-01" :status "FAIL" :description "monitor n/a"}]}
                       "2020-01-01")))
    (is (= nil
           (u/expired? {:results [{:isodate "2000-01-01" :status "PASS"}]}  "2002-07-01")))
    (is (= "Item missing. Last seen 2000-01-01"
           (u/expired? {:results [{:isodate "2000-01-01" :status "PASS"}]}  "2002-08-01")))))

