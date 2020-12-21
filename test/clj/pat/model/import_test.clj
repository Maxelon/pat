(ns pat.model.import-test
  (:require [clojure.test :refer :all]
            [pat.model.import :as mi]))

(deftest test-isodate
  (is (= "2020-01-30"
         (mi/isodate "30-Jan-2020"))))

(deftest test-import-formatting
  ;;legacy
  (is (= {:tag         "85A"
          :description "Ext Lead"
          :type        "LEAKAGE"
          :isodate     "2018-09-15"
          :status      "PASS"
          :inspector   "Malcolm"
          :wiring nil
          :leakage nil
          :insulation nil
          :image nil
          :earth nil}
         (select-keys (mi/format-result
           {:tag :result,
            :attrs nil,
            :content [{:tag :assetId, :attrs nil, :content ["85A"]}
                      {:tag :description, :attrs nil, :content ["Ext Lead"]}
                      {:tag :lastTestDate, :attrs nil, :content ["2018-09-15"]}
                      {:tag :testType, :attrs nil, :content ["LEAKAGE"]}
                      {:tag :testResult, :attrs nil, :content ["PASS"]}
                      {:tag :earthResult, :attrs nil, :content ["N/A"]}
                      {:tag :insulationResult, :attrs nil, :content ["N/A"]}
                      {:tag :leakageResult, :attrs nil, :content ["N/A"]}
                      {:tag :wiringResult, :attrs nil, :content ["N/A"]}
                      {:tag :userName, :attrs nil, :content ["Malcolm"]}]})
                      [:tag :description :type :isodate :status :image
                       :earth :insulation :leakage :wiring :inspector])))
  ;;Android with test
  (is (= {:description "75 E DVD PLAYER",
          :inspector "Allan",
          :leakage "<0.10",
          :type "INSULATION",
          :isodate "2019-12-29",
          :status "PASS",
          :insulation ">19.99",
          :tag "0542"
          :wiring nil
          :earth nil
          :image nil}
         (select-keys (mi/format-result
                        {:tag :result,
                         :attrs nil,
                         :content [{:tag :version, :attrs nil, :content ["0.01"]}
                                   {:tag :uuid, :attrs nil, :content ["52b74519-7866-4400-81e3-ab92e9a7aedb"]}
                                   {:tag :assetId, :attrs nil, :content ["0542"]}
                                   {:tag :description, :attrs nil, :content ["75 E DVD PLAYER"]}
                                   {:tag :lastTestDate, :attrs nil, :content ["29-Dec-2019"]}
                                   {:tag :nextTestDate, :attrs nil, :content ["29-Dec-2020"]}
                                   {:tag :image, :attrs nil, :content nil}
                                   {:tag :testType, :attrs nil, :content ["INSULATION"]}
                                   {:tag :testResult, :attrs nil, :content ["PASS"]}
                                   {:tag :commentDescription, :attrs nil, :content nil}
                                   {:tag :earthResult, :attrs nil, :content ["null"]}
                                   {:tag :insulationResult, :attrs nil, :content [">19.99"]}
                                   {:tag :leakageResult, :attrs nil, :content ["<0.10"]}
                                   {:tag :wiringResult, :attrs nil, :content ["OPEN"]}
                                   {:tag :earthStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :insulationStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :leakageStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :wiringStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :userName, :attrs nil, :content ["Allan"]}
                                   {:tag :testerType, :attrs nil, :content ["PT100"]}
                                   {:tag :serialNumber, :attrs nil, :content ["34K-0345"]}]})
                      [:tag :description :type :isodate :status :image
                       :earth :insulation :leakage :wiring :inspector])))
  ;;Android without test
  (is (= {:description "200a. Dell",
          :inspector "Allan",
          :type "EARTH",
          :isodate "2020-01-01",
          :status "PASS",
          :tag "0557"
          :wiring nil
          :earth nil
          :image nil
          :insulation nil
          :leakage nil}
         (select-keys (mi/format-result
                        {:tag :result,
                         :attrs nil,
                         :content [{:tag :version, :attrs nil, :content ["0.01"]}
                                   {:tag :uuid, :attrs nil, :content ["e33f7e35-0eeb-4af8-ba23-815ea65e867b"]}
                                   {:tag :assetId, :attrs nil, :content ["0557"]}
                                   {:tag :description, :attrs nil, :content ["200a. Dell"]}
                                   {:tag :lastTestDate, :attrs nil, :content ["01-Jan-2020"]}
                                   {:tag :nextTestDate, :attrs nil, :content ["01-Jan-2021"]}
                                   {:tag :image, :attrs nil, :content nil}
                                   {:tag :testType, :attrs nil, :content ["EARTH"]}
                                   {:tag :testResult, :attrs nil, :content ["PASS"]}
                                   {:tag :commentDescription, :attrs nil, :content nil}
                                   {:tag :earthResult, :attrs nil, :content ["null"]}
                                   {:tag :insulationResult, :attrs nil, :content ["null"]}
                                   {:tag :leakageResult, :attrs nil, :content ["null"]}
                                   {:tag :wiringResult, :attrs nil, :content ["OPEN"]}
                                   {:tag :earthStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :insulationStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :leakageStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :wiringStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :userName, :attrs nil, :content ["Allan"]}
                                   {:tag :testerType, :attrs nil, :content ["PT100"]}
                                   {:tag :serialNumber, :attrs nil, :content ["34K-0345"]}]})
                      [:tag :description :type :isodate :status :image
                       :earth :insulation :leakage :wiring :inspector])))
  ;;Apple file with image
  (is (= {:description "dummy asset",
          :inspector "Ken",
          :type "EARTH",
          :isodate "2020-08-30",
          :status "PASS",
          :tag "dummy"
          :image "20200830_125508.jpg"
          :wiring nil
          :leakage nil
          :earth nil
          :insulation nil}
         (select-keys (mi/format-result
                        {:tag :result,
                         :attrs nil,
                         :content [{:tag :version, :attrs nil, :content ["0.01"]}
                                   {:tag :uuid, :attrs nil, :content ["70BBC7F2-7A3A-4755-B6FF-CEFEC83CB2A5"]}
                                   {:tag :assetId, :attrs nil, :content ["dummy"]}
                                   {:tag :description, :attrs nil, :content ["dummy asset"]}
                                   {:tag :lastTestDate, :attrs nil, :content ["30-Aug-2020"]}
                                   {:tag :nextTestDate, :attrs nil, :content ["30-Aug-2021"]}
                                   {:tag :image, :attrs nil, :content ["20200830_125508.jpg"]}
                                   {:tag :testType, :attrs nil, :content ["EARTH"]}
                                   {:tag :testResult, :attrs nil, :content ["PASS"]}
                                   {:tag :commentDescription, :attrs nil, :content nil}
                                   {:tag :earthResult, :attrs nil, :content ["N/A"]}
                                   {:tag :insulationResult, :attrs nil, :content ["N/A"]}
                                   {:tag :leakageResult, :attrs nil, :content ["N/A"]}
                                   {:tag :wiringResult, :attrs nil, :content ["OPEN"]}
                                   {:tag :earthStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :insulationStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :leakageStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :wiringStatus, :attrs nil, :content ["N/A"]}
                                   {:tag :userName, :attrs nil, :content ["Ken"]}
                                   {:tag :testerType, :attrs nil, :content ["PT100"]}
                                   {:tag :serialNumber, :attrs nil, :content ["34K-0345"]}]})
                      [:tag :description :type :isodate :status :image
                       :earth :insulation :leakage :wiring :inspector])))
  )
(deftest test-asset
  (testing "extract-tags"
    (let [result {:tag         "001"
                  :description "An asset66 and 2B"
                  :type        "EARTH"
                  :status      "PASS"
                  :isodate     "2018-01-01"
                  :inspector   "Ken"
                  :comment "I 23a am 2"}]
      (is (= '("001" "66" "2B" "23A" "2")
             (mi/extract-tags result))))))

(deftest test-guess-lid
  ;if no tag matched, returns "AC" default
  (is (= "AC"
         (mi/guess-lid "./test/data/9095.zip"))))

