(ns bandido-clojure.core-test
  (:require
            [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [bandido-clojure.core :refer [init-table]]
            [bandido-clojure.specs :as specs]
            [clojure.spec.test.alpha :as stest]))

(deftest init-table-test
  (testing "init-table should return a Bdd with a table t with two elements")
    (let [t (init-table 5)
          t-size (count t)]
      (is (= 2  t-size))))
