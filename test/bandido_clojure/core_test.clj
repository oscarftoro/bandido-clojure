(ns bandido-clojure.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [bandido-clojure.core :refer [init-table mk1 mk1a v low high apply* apply2* map->Bdd ]]
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
  (deftest low-test
    (testing "given a bdd and a u returns the corresponding low")
    (let [l4 (low 4 bdd-test)
          l3 (low 3 bdd-test)
          l2 (low 2 bdd-test)
          l1 (low 1 bdd-test)
          l5 (low 5 bdd-test)]
      (is (and (= 0 l4)
               (= 0 l2)
               (= 1 l3)
               (= 0 l5)))))
  (deftest value-test
    (testing "given a bdd and a u returns the corresponding value")
    (let [v4 (v 4 bdd-test)
          v3 (v 3 bdd-test)
          v2 (v 2 bdd-test)
          v1 (v 1 bdd-test)
          v5 (v 5 bdd-test)]
      (is (and (= 2 v4)
               (= 3 v2)
               (= 2 v3)
               (= 1 v5)))))
  (deftest high-test
    (testing "given a bdd and a u returns the corresponding high")
    (let [h4 (high 4 bdd-test)
          h3 (high 3 bdd-test)
          h2 (high 2 bdd-test)
          h1 (high 1 bdd-test)
          h5 (high 5 bdd-test)]
      (is (and (= 1 h4)
               (= 1 h2)
               (= 0 h3)
               (= 2 h5))))))

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

(deftest mk1a-should-make-test
  (testing "that mk1a add a new node to the table")
  (let [bdd (init-table 4)
        bdd* (mk1a [4 1 0] bdd)
        t (:t bdd*)
        t-size (count t)]
    (is (= 3 t-size))))

(deftest mk1a-should-return-the-same-bdd-when-l-eq-h-test
  (testing "mk1a do nothing when l = h but returns the same bdd"
    (let [bdd (init-table 4)
          bdd* (mk1a [4 1 1] bdd)
          t (:t bdd*)
          t-size (count t)]
      (is (= 2 t-size)))))

(deftest mk1a-should-be-efficient-test
  (testing "mk1a returns the value saved in table h when it corresponds"
    (let [bdd (init-table 4)
          bdd1 (mk1a [4 1 0] bdd)
          bdd2 (mk1a [4 0 1] bdd1)
          bdd3 (mk1a [4 1 0] bdd2)
          t (:t bdd3)
          t-size (count t)]
      (is (= 4 t-size)))))


(deftest apply-should-behave-as-expected
  (testing "apply should apply a logic formula to a bdd")
  (let [bdd (init-table 3)
        bdd1 (mk1 [3 0 1] bdd)
        bdd2 (mk1 [2 1 0] bdd1)
        bdd3 (mk1 [2 0 1] bdd2)
        bdd4 (mk1 [1 0 2] bdd3)
        bdd5 (mk1 [1 3 4] bdd4)
        bdd6 (apply* :and 5 6 bdd5)
        
        expected (map->Bdd {:t {0 [4 0 0]
                                1 [4 1 1]
                                2 [3 0 1]
                                3 [2 1 0]
                                4 [2 0 1]
                                5 [1 0 2]
                                6 [1 3 4]
                                7 [2 0 2]
                                8 [1 0 7]}
                            :uid 8
                            :luid 8})]
    (is (= expected bdd6 ))))


(use-fixtures :each setup )
