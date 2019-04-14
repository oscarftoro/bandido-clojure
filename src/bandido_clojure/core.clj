(ns bandido-clojure.core                
  (:require [clojure.set           :as set]
            [bandido-clojure.specs :as bspec]
            [clojure.spec.alpha    :as s]))

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

(comment
  "A BDD is a tuple (t, uid) such that `t` is a map of type `uid -> [i l h]`
   and `uid is an integer that stands for unique id. This tuple is represented in this case
   as a record.
   The variable `i` represents the index of a variable x_i,
   for instance, variables are represented as x_1, x_2, x_3...x_n. Variable `l` represents 
   the low branch whereas `h`, the high branch. Both are integers of type uid.")

(defrecord Bdd [t uid])

(comment
  "Bdds is a record that represents more than one boolean expression, the roots
   of those expressions are the uid elements of a vector of uids.")

(defrecord Bdds [t uids])

(defn init-table [var-num]
  "Initialise a record representing a node containing a table(map) u -> [ i l h].
   It returns a Bdd record where:
   `:t` is the unique table that represent a single, multirooted graph.
   `:uid` is the current result which is 1 at the begining.
 
   The algorithms are expecting to return different values of `:u` but since this is 
   is for the initialisation phase, the value of `:u` is nil"
  
  {:pre [(s/valid? ::bspec/vid var-num)]}
  (map->Bdd {:t     {0 [(inc var-num) 0 0] 1 [(inc var-num) 1 1]}
             :uid 1}))


(defn init-table2 [var-num]
  "Initialise a table that works with maps instead of records"
  {:t     {0 [var-num 0 0] 1 [var-num 1 1]}
   :uid 1})

;;; helper functions to extract values of ite vectors
;;; given a ite vector and a t table, returns the corresponding value

(defn v    [u t] (-> (t u) (first)))
(defn low  [u t] (-> (t u) (second)))
(defn high [u t] (-> (t u) (nth 2)))

(defn bdd->ht [bdd]
  "Takes a partial result and returns a map `h` which is the inverse of `t`"
  {:pre [(s/valid? ::bspec/bdd bdd)]}
  (clojure.set/map-invert (:t bdd)))

(defn bdd-map->ht [bdd-map]
  "Takes a map representing a bdd and returns a map `h`; the inverse of `t`"
  (clojure.set/map-invert (:t bdd-map)))


(defn- pack-operation-uid [operation vals bdd] ; naming and placing this thing is challenging 
  "returns a partial result containing the relevant data after applying an operation
  on u given some values and a context defined by a Bdd"  
  (let [uid operation
        t1 (conj (:t bdd) [uid vals])]
    (map->Bdd
     {:t     t1
      :uid uid})))


(defn- update-uid [operation bdd] ; same here 
  "returns an updated Bdd containing the relevant data after applying an operation
  on u "
  (assoc bdd :uid operation))

;;; ######################################################## 
;;; ###                 MAKE ALGORITHM                   ###
;;; ########################################################

(defn mk1 [[i l h] bdd]
  "The core of building reduced BDDs.
   Make a new entry representing a node in table `t`.
   A Bdd is returned." 
  (if (= l h) bdd
      (let [ht (bdd->ht bdd)]
        (if (contains? ht [i l h])
          (assoc bdd :uid (ht [i l h])) 
          (pack-operation-uid (-> bdd (:uid) (inc)) [i l h] bdd)))))

;; Mk using cond instead of if do not gain soo much 
(defn mk1a [[i l h] bdd]
  (cond
    (= l h) bdd
    :else (let [ht (bdd->ht bdd)]
            (if (contains? ht [i l h])
              (assoc bdd :uid (ht [i l h])) 
              (let [u  (inc (:uid bdd))
                    t1 (conj (:t bdd) [u [i l h]])]
                (map->Bdd
                 {:t     t1
                  :uid u}))))))


(defn mk2 [[i l h] m]
  "The essence of building reduced BDDs.
   Make a new entry representing a node in table t.
   A map result is returned" 
  (if (= l h)
    m
    (let [ht (bdd-map->ht m)]
      (if (contains? ht [i l h])
        (assoc m :u (ht [i l h])) ; an updated map
        (let [u  (inc (:max-u m))
              t1 (conj (:t m) [u [i l h]])]
          {:t     t1
           :max-u u})))))

;;; a mk3 could be implemented using the map metadata of the object to
;;; store var-num, this will require an init-table3 that uses with-meta 

;;; ######################################################## 
;;; ###                APPLY ALGORITHM                   ###
;;; ########################################################


(defn- eval-op
  [op u1 u2]
  (case op
    :and (and* u1 u2)
    :or  (or* u1 u2)))

(defn- memo [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem [(nth args 1) (nth args 2)])]
        (val e)
        (let [bdd (apply f args)
              uid (:uid bdd)]
          (swap! mem assoc [(nth args 1) (nth args 2)] uid)
          (println "uid " uid ": [" (nth args 1) " "  (nth args 2) "]")
          bdd)))))

(defn- app [op u1 u2 bdd]
  (let [t (:t bdd)]
    (cond
      (-> (contains? #{0 1} u1)
          (and (contains? #{0 1} u2))) (update-uid (eval-op op u1 u2) bdd)
      (= (v u1 t) (v u2 t)) (let [low  (-> (app op (low  u1 t) (low  u2 t) bdd) :uid)
                                  high (-> (app op (high u1 t) (high u2 t) bdd) :uid)]
                              (mk1 [(v u1 t) low high] bdd))
      
      (< (v u1 t) (v u2 t)) (let [low  (-> (app op (low  u1 t) u2 bdd) :uid)
                                  high (-> (app op (high u1 t) u2 bdd) :uid)]
                              (mk1 [(v u1 t) low high] bdd))
      
      :else  (let [low  (-> (app op u1 (low  u2 t) bdd) :uid)
                   high (-> (app op u1 (high u2 t) bdd) :uid)]
               (mk1 [(v u2 t) low high] bdd)))))


(def apply* (memo app))


