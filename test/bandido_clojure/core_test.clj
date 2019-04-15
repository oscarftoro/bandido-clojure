(ns bandido-clojure.core-test
  (:require
            [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [bandido-clojure.core :refer [init-table mk1]]
            [bandido-clojure.specs :as specs]
            [clojure.spec.test.alpha :as stest]))

(deftest init-table-test
  (testing "init-table should return a Bdd with a table t with two elements")
    (let [bdd (init-table 5)
          t (:t bdd)
          t-size (count t)]
      (is (= 2  t-size))))

(deftest mk-should-make
  (testing "that mk add a new node to the table")
  (let [bdd (init-table 4)
        bdd* (mk1 [4 1 0] bdd)
        t (:t bdd*)
        t-size (count t)]
    (is (= 3 t-size))))
