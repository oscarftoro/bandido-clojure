(ns bandido-clojure.specs  
  (:require [clojure.spec.alpha :as s]))



(s/def :dk.nqn.bandido-clojure/b   #{0 1})
(s/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))
(s/def :dk.nqn.bandido-clojure/var :dk.nqn.bandido-clojure/uid)
(s/def :dk.nqn.bandido-clojure/low :dk.nqn.bandido-clojure/uid)
(s/def :dk.nqn.bandido-clojure/hgh :dk.nqn.bandido-clojure/uid)

(s/def :dk.nqn.bandido-clojure/ite (s/coll-of int? :kind vector? :count 3))

(s/def :dk.nqn.bandido-clojure/t   (s/map-of int? :dk.nqn.bandido-clojure/node))
(s/def :dk.nqn.bandido-clojure/h   (s/map-of :dk.nqn.bandido-clojure/node int?))
                                      
;; base fun(S/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))ctions
