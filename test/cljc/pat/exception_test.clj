(ns pat.exception_test
  (:require [clojure.test :refer :all]
            [pat.exception :as me]
            [java-time :as jt]))

(deftest test-status-error?
  (is (= "Result status is: FAIL"
         (me/status-error? {:status "FAIL"})))
  (is (= nil
         (me/status-error? {:status "PASS"}))))

(deftest test-earth-error?
  (is (= nil
         (me/earth-error? {} {:class 2})))
  (is (= nil
         (me/earth-error? {:earth "PASS"} {:earth-lim 0.1})))
  (is (= nil
         (me/earth-error? {:earth "0.1"} {:earth-lim 0.1})))
  (is (= "High earth value: 0.2"
         (me/earth-error? {:earth "0.2"} {:earth-lim 0.1})))
  (is (= "Failed earth result: FAIL"
         (me/earth-error? {:earth "FAIL"} {:earth-lim 0.1}))))

(deftest test-insulation-error?
  (is (= nil
         (me/insulation-error? {:insulation "PASS"} {:class 1})))
  (is (= "No insulation result."
         (me/insulation-error? {} {:insulation-lim 1.0})))
  (is (= nil
         (me/insulation-error? {:insulation "PASS"} {:insulation-lim 1.0})))
  (is (= "Failed insulation result: FAIL"
         (me/insulation-error? {:insulation "FAIL"} {:insulation-lim 1.0})))
  (is (= nil
         (me/insulation-error? {:insulation "1.0"} {:insulation-lim 1.0})))
  (is (= "Low insulation value: 0.9"
         (me/insulation-error? {:insulation "0.9"} {:insulation-lim 1.0}))))

(deftest test-leakage-error?
  (is (= nil
         (me/leakage-error? {} {:leakage 0.25})))
  (is (= nil
         (me/leakage-error? {:leakage "PASS"} {:leakage-lim 0.25})))
  (is (= "Failed leakage result: FAIL"
         (me/leakage-error? {:leakage "FAIL"} {:leakage-lim 0.25})))
  (is (= nil
         (me/leakage-error? {:leakage "0.25"} {:leakage-lim 0.25})))
  (is (= "High leakage value: 0.26"
         (me/leakage-error? {:leakage "0.26"} {:leakage-lim 0.25}))))

(deftest test-wiring-error?
  (is (= nil
         (me/wiring-error? {:wiring nil})))
  (is (= nil
         (me/wiring-error? {:wiring "GOOD"})))
  (is (= "Wiring error: PASS"
         (me/wiring-error? {:wiring "PASS"}))))

(deftest test-error?
  (testing ":status"
    (is (= nil
           (me/error? {:status "PASS" :earth "PASS" :insulation "PASS" :leakage "PASS" :wiring "GOOD"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25})))
    (is (= "Result status is: FAIL"
           (me/error? {:status "FAIL" :earth "PASS" :insulation "PASS" :leakage "PASS" :wiring "GOOD"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25}))))

  (testing ":earth"
    (is (= "Failed earth result: FAIL"
           (me/error? {:status "PASS" :earth "FAIL" :insulation "PASS" :leakage "PASS" :wiring "GOOD"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25})))
    (is (= "Failed earth result: FAIL"
           (me/error? {:status "PASS" :earth "FAIL" :insulation "FAIL" :leakage "FAIL" :wiring "FAIL"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25}))))

  (testing ":insulation"
    (is (= "Failed insulation result: FAIL"
           (me/error? {:status "PASS" :earth "PASS" :insulation "FAIL" :leakage "PASS" :wiring "GOOD"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25})))
    (is (= "Failed insulation result: FAIL"
           (me/error? {:status "PASS" :earth "PASS" :insulation "FAIL" :leakage "FAIL" :wiring "FAIL"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25}))))

  (testing ":leakage"
    (is (= "Failed leakage result: FAIL"
           (me/error? {:status "PASS" :earth "PASS" :insulation "PASS" :leakage "FAIL" :wiring "GOOD"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25})))
    (is (= "Failed leakage result: FAIL"
           (me/error? {:status "PASS" :earth "PASS" :insulation "PASS" :leakage "FAIL" :wiring "FAIL"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25}))))

  (testing ":wiring"
    (is (= "Wiring error: FAIL"
           (me/error? {:status "PASS" :earth "PASS" :insulation "PASS" :leakage "PASS" :wiring "FAIL"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25}))))

  (testing ":assessment"
    (is (= nil
           (me/error? {:status "FAIL" :assessment "PASS"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25})))
    (is (= "Test"
           (me/error? {:status "PASS" :assessment "FAIL" :reason "Test"}
                      {:class 1 :earthed true :earth-lim 0.1 :insulation-lim 1.0 :leakage-lim 0.25})))))

(deftest test-overdue?
  (testing "missed test"
    (is (= "No test recorded."
           (me/overdue? {:description "IT"
                       :results [{:isodate "2021-01-01"}]}
                        "1000-02-01")))
    (is (= "Test missed at last inspection: 2021-01-01"
           (me/overdue? {:description "IT"
                         :results     [{:isodate "2021-01-01"}
                                       {:isodate "2018-01-01" :insulation "x"}]
                                       }
                        "1000-02-01"))))
  (testing "missed inspection"
    (is (= nil
           (me/overdue? {:description "IT"
                       :results [{:isodate "2018-01-01" :insulation "x" :type "INSULATION"}]}
                        "2020-02-01")))
    (is (= "Inspection overdue."
           (me/overdue? {:description "IT"
                         :results [{:isodate "2018-01-01" :insulation "x" :type "INSULATION"}]}
                        "2020-02-02")))
    (is (= nil
           (me/overdue? {:description "IT"
                         :results [{:isodate "2021-01-01"}
                                   {:isodate "2018-01-01" :insulation "x" :type "INSULATION"}]}
                        "2021-01-01")))))


(deftest test-add-failure
  (is (= "Result status is: FAIL"
         (:failure (me/assoc-failure {:description "IT"
                                    :results [{:status "PASS"}
                                              {:insulation "x" :status "FAIL"}]} ))))
  (is (= nil
         (:failure (me/assoc-failure {:description "IT"
                                    :results [{:status "PASS"}
                                              {:status "FAIL" :assessment "PASS" :insulation "x"}] } ))))
  (is (= "test"
         (:failure (me/assoc-failure {:description "IT"
                                    :results [{:status "PASS" :assessment "FAIL" :reason "test"}
                                              {:status "FAIL" :assessment "PASS" :insulation "x"} ]}))))
  (is (= nil
         (:failure (me/assoc-failure {:description "IT"
                                    :last-test {:status "FAIL" :assessment "PASS"}
                                    :results [{:status "FAIL" :assessment "PASS" :reason "test"}
                                              {:status "FAIL" :assessment "PASS" :insulation "x "}]}))))
  (is (= "Result status is: nil"
         (:failure (me/assoc-failure {:description "SIT"})))))