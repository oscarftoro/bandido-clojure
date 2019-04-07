(ns bandido-clojure.core
  (:require [clojure.set        :as set]
            [bandido-clojure.specs :as bspec]
            [clojure.spec.alpha :as s]))


(defn const [b] b)

;;; ######################################################## 
;;; ###            PRIMITIVES ON 0 and 1                 ###
;;; ########################################################

(defn- bdd-not [b]
  (case b
    1 0
    0 1))(s/def ::partial-result (s/keys :req-un [::t
                                                  ::uid
                                                  ::uid]))


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
   When f is true returns g otherwise h.
   f is true if f and g is true and false when f is false and h is true"
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

;;; ######################################################## 
;;; ###                 CONFIGURATION                    ###
;;; ########################################################

(defrecord Bdd [t max-u ])
(defrecord PartialResult [t max-u u])

(defn init-table [var-num]
  "Initialise a record representing a node containing a table(map) u -> [ i l h].
   It returns a PartialResult record where:
   `:t` is the unique table that represent a single, multirooted graph.
   `:max-u` is the largest number representing the last variable of the bdd.
   `:u` is the current result which is nil at the begining.
 
   The algorithms are axpecting to return different values of :u but since this is 
   is for the initialisation phase, the value of :u is nil"
  
  {:pre [(s/valid? ::bspec/vid var-num)]}
  (map->PartialResult {:t     {0 [(inc var-num) 0 0] 1 [(inc var-num) 1 1]}
                       :max-u 1}))


(defn init-table2 [var-num]
  "Initialise a table that works with maps instead of records"
  {:t     {0 [var-num 0 0] 1 [var-num 1 1]}
   :max-u 1
   :u     nil})

;;; helper functions to extract values of ite vectors
;;; given a ite vector and a t table, returns the corresponding value

(defn v    [u t] (-> (t u) (first))
(defn low  [u t] (-> (t u) (second))
(defn high [u t] (-> (t u) (nth 2))

(defn partial-result->ht [pr]
  "Takes a partial result and returns a map h which is the inverse of t"
  {:pre [(s/valid? ::bspec/partial-result pr)]}
  (clojure.set/map-invert (:t pr)))

(Defn map->ht [m]
  "Takes a map and returns a map h; the inverse of t"
  (clojure.set/map-invert (:t m)))


(defn- pack-operation-u [operation vals pr]
  "returns a partial result containing the relevant data after applying an operation
  on u given some values and a context defined by a partial result pr"  
  (let [u operation
        t1 (conj (:t pr) [u vals])]
    (map->PartialResult
     {:t     t1
      :max-u u})))
  
;;; ######################################################## 
;;; ###                 MAKE ALGORITHM                   ###
;;; ########################################################
  
(defn mk1 [[i l h] pr]
  "The core of building reduced BDDs.
   Make a new entry representing a node in table t.
   A partial result is returned" 
  (if (= l h) pr
    (let [ht (partial-result->ht pr)]
      (if (contains? ht [i l h])
        (assoc :u ([i l h] ht) pr) ; a partial-result 
        (pack-operation-u (-> pr (:max-u) (inc)) [i l h] pr)))))
      
      ;; Mk using cond instead of if do not gain soo much 
(defn mk1a [[i l h] pr]
  (cond
    (= l h) pr
    :else (let [ht (partial-result->ht pr)]
            (if (contains? ht [i l h])
              (assoc :u ([i l h] ht) pr) ; a partial-result
              (let [u  (inc (:max-u pr))
                    t1 (conj (:t pr) [u [i l h]])]
                (map->PartialResult
                 {:t     t1
                  :max-u u}))))))
      

(defn mk2 [[i l h] m]
  "The essence of building reduced BDDs.
   Make a new entry representing a node in table t.
   A map result is returned" 
  (if (= l h)
    m
    (let [ht (map->ht m)]
      (if (contains? ht [i l h])
        (assoc :u ([i l h] ht) m) ; an updated map
        (let [u  (inc (:max-u m))
              t1 (conj (:t m) [u [i l h]])]
          {:t     t1
           :max-u u})))))
  
;;; a mk3 could be imnplemented using the map metadata of the object to
;;; store var-num this will require a init-table3 that uses with-meta 

;;; ######################################################## 
;;; ###                APPLY ALGORITHM                   ###
;;; ########################################################


(defn- eval-op
  [op u1 u2]
  (case op
    :and (and* u1 u2)
    :or  (or* u1 u2))))

(defn- app [op u1 u2 pr g]
  (let [t (:t pr)]  
    (cond
      (contains? g [u1 u2]) (g [u1 u2])
      (-> (contains? #{0 1} u1)
          (and (contains? #{0 1} u2))) (eval-op op u1 u2)
      (= (v u1 t) (v u2 t)) 
      (< (v u1 t) (v u2 t))
      :else 1))) 

 (defn apply [op u1 u2 pr]
   (let [g {}]
     (app op u1 u2 pr g)))
  
