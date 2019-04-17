(ns bandido-clojure.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [bandido-clojure.core :refer [init-table mk1 low]]
   [bandido-clojure.specs :as specs]
   [clojure.spec.test.alpha :as stest]))


(defn setup [f]
  (def bdd-test (let [bdd (init-table 3)
                      bdd1 (mk1 [3 0 1] bdd)
                      bdd2 (mk1 [2 1 0] bdd1)
                      bdd3 (mk1 [2 0 1] bdd2)
                      bdd4 (mk1 [1 0 2] bdd3) 
                      bdd5 (mk1 [1 3 4] bdd4)] bdd5))
  (f))

(deftest t-table-function-test
  (testing "tests related to table t")
  (deftest value-test
    (testing "given a bdd and a u returns the corresponding value")
    (let [l (low 4 bdd-test)]
      (is (= 0 l)))))


(deftest init-table-test
  (testing "init-table should return a Bdd with a table t with two elements")
  (let [bdd (init-table 5)
        t (:t bdd)
        t-size (count t)]
    (is (= 2  t-size))))

(deftest mk-should-make-test
  (testing "that mk add a new node to the table")
  (let [bdd (init-table 4)
        bdd* (mk1 [4 1 0] bdd)
        t (:t bdd*)
        t-size (count t)]
    (is (= 3 t-size))))

(deftest mk-should-return-the-same-bdd-when-l-eq-h-test
  (testing "mk do nothing when l = h but returns the same bdd"
    (let [bdd (init-table 4)
          bdd* (mk1 [4 1 1] bdd)
          t (:t bdd*)
          t-size (count t)]
      (is (= 2 t-size)))))

(deftest mk-should-be-efficient-test
  (testing "mk returns the value saved in table h when it corresponds"
    (let [bdd (init-table 4)
          bdd1 (mk1 [4 1 0] bdd)
          bdd2 (mk1 [4 0 1] bdd1)
          bdd3 (mk1 [4 1 0] bdd2)
          t (:t bdd3)
          t-size (count t)]
      (is (= 4 t-size)))))


(use-fixtures :each setup )
