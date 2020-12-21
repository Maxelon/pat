(ns pat.asset-test
  (:require [clojure.test :refer :all]
            [pat.asset :as a]))

(deftest test-assoc-summary
  (is (= {:description     "none",
          :results         [{:tag "A", :isodate "2000-01-03", :insulation nil}
                            {:tag "B", :isodate "2000-01-02", :insulation nil}
                            {:tag "C", :isodate "2000-01-01", :insulation nil}],
          :env-type        :portable,
          :tag             "A",
          :tags            ["A" "B" "C"],
          :class           1,
          :last-inspection {:tag "A", :isodate "2000-01-03", :insulation nil},
          :last-test       nil}
         (a/assoc-summary {:description "none"
                           :results     [{:tag "A" :isodate "2000-01-03" :insulation nil}
                                         {:tag "B" :isodate "2000-01-02" :insulation nil}
                                         {:tag "C" :isodate "2000-01-01" :insulation nil}]})))
  (is (= {:description     "none",
          :results         [{:tag "A", :isodate "2000-01-03", :insulation nil}
                            {:tag "B", :isodate "2000-01-02", :insulation "not nil"}
                            {:tag "C", :isodate "2000-01-01", :insulation nil}],
          :env-type        :portable,
          :tag             "A",
          :tags            ["A" "B" "C"],
          :class           1,
          :last-inspection {:tag "A", :isodate "2000-01-03", :insulation nil},
          :last-test       {:tag "B" :isodate "2000-01-02" :insulation "not nil"}}
         (a/assoc-summary {:description "none"
                           :results     [{:tag "A" :isodate "2000-01-03" :insulation nil}
                                         {:tag "B" :isodate "2000-01-02" :insulation "not nil"}
                                         {:tag "C" :isodate "2000-01-01" :insulation nil}]})))

  (is (= {:description     "fixed",
          :results         [{:tag "A", :isodate "2000-01-03", :insulation nil :type "INSULATION"}
                            {:tag "B", :isodate "2000-01-02", :insulation "not nil" :type "INSULATION"}
                            {:tag "C", :isodate "2000-01-01", :insulation "not nil" :type "INSULATION"}],
          :env-type        :fixed,
          :tag             "A",
          :tags            ["A" "B" "C"],
          :class           2,
          :last-inspection {:tag "A", :isodate "2000-01-03", :insulation nil :type "INSULATION"},
          :last-test       {:tag "B" :isodate "2000-01-02" :insulation "not nil" :type "INSULATION"}}
         (a/assoc-summary {:description "fixed"
                           :results     [{:tag "A" :isodate "2000-01-03" :insulation nil :type "INSULATION"}
                                         {:tag "B" :isodate "2000-01-02" :insulation "not nil" :type "INSULATION"}
                                         {:tag "C" :isodate "2000-01-01" :insulation "not nil" :type "INSULATION"}]})))
  (is (= {:description     "fixed",
          :results         [{:tag "A", :isodate "2000-01-03", :insulation nil :type "INSULATION"}
                            {:tag "B", :isodate "2000-01-02", :insulation nil :type "INSULATION"}
                            {:tag "C", :isodate "2000-01-01", :insulation "not nil" :type "INSULATION"}],
          :env-type        :fixed,
          :tag             "A",
          :tags            ["A" "B" "C"],
          :class           2,
          :last-inspection {:tag "A", :isodate "2000-01-03", :insulation nil :type "INSULATION"},
          :last-test       {:tag "C" :isodate "2000-01-01" :insulation "not nil" :type "INSULATION"}}
         (a/assoc-summary {:description "fixed"
                           :results     [{:tag "A" :isodate "2000-01-03" :insulation nil :type "INSULATION"}
                                         {:tag "B" :isodate "2000-01-02" :insulation nil :type "INSULATION"}
                                         {:tag "C" :isodate "2000-01-01" :insulation "not nil" :type "INSULATION"}]})))
  (is (= {:description     "none"
          :env-type        :portable
          :tag             nil
          :tags            ()
          :class           1
          :last-inspection nil
          :last-test       nil}
         (a/assoc-summary {:description "none"}))))
