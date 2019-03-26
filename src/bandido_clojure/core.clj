(ns bandido-clojure.core
  (:require [clojure.set        :as set]
            [bandido-clojure.specs :as s]))


(defn const [b] b)

;;; ######################################################## 
;;; ###            PRIMITIVES ON 0 and 1                 ###
;;; ########################################################

(defn- bdd-not [b]
  (case b
    1 0
    0 1))

(defn- bdd-or [b c]
  (case b
    1 1
    0 (case c
        1 1
        0 0)))

(defn- bdd-and [b c]
  (case b
    0 0
    1 (bdd-or 0 c)))    

;;; ######################################################## 
;;; ###                BDDs FUNCTIONS                    ###
;;; ########################################################

;;; naive implementation of ite
(defn ite [f g h]
  "Three argument operation that stands for If-then-else.
   When f is true returns g otherwise h"
  (bdd-or (bdd-and f g) (bdd-and (bdd-not f) h)))

(defn and* [f g]
  (ite f g 0))

(defn or* [f g]
  (ite f 1 g))

(defn xor* [f g]
  (ite f (bdd-not g) g))

(defn nor* [f g]
  (ite f 0 (bdd-not g)))

(defn xnor* [f g]
  (ite f g (bdd-not g)))

(defn not* [f]
  (ite f 0 1))

(defn nand* [f g]
  (ite f g 1))

(defn zero* [] 0)

(defn one* [] 1)

(defrecord Bdd [t prev-u ])
(defrecord PartialResult [t prev-u u])

(defn mk [[i l h] partial-result]
  (if (= l h)
    partial-result
    (let [ht (set/map-invert (:t partial-result))]
      (if (contains? ht [i l h])
        ([i  l h] ht)
        (let [:u  (inc (:prev-u partial-result))
              :t1 (conj t [:u [i l h]])]
          (map->PartialResult
           {:t      t1
            :prev-u u}))))))


