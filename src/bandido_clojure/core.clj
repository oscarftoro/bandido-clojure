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


(defn ite [f g h]
  "Three argument operation that stands for If-then-else.
   When f is true returns g otherwise h"
  (bdd-or (bdd-and f g) (bdd-and (bdd-not f) h)))

(defn and* [f g]
  (ite f g 0))

