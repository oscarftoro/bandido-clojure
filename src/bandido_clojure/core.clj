(ns bandido-clojure.core
  (:require [clojure.spec.alpha :as s]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


;;specs
(s/def :dk.nqn.bandido-clojure/b #{0 1})
(s/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))
(s/def :dk.nqn.bandido-clojure/var (s/and int? #(> % 0)))
;(s/def :dk.nqn.bandido-clojure/ite
 ; (s/keys :req-un []))
;; base fun(S/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))ctions
(defn const [b] b)

;; ######################################################## 
;; ###            PRIMITIVES ON 0 and 1                 ###
;; ########################################################

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

;; ######################################################## 
;; ###                BDDs FUNCTIONS                    ###
;; ########################################################

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
