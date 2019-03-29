(ns bandido-clojure.specs  
  (:require [clojure.spec.alpha :as s]))



(s/def :dk.nqn.bandido-clojure/b   #{0 1})                                      ; boolean value
(s/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))                       ; unique id
(s/def :dk.nqn.bandido-clojure/vid :dk.nqn.bandido-clojure/uid)                 ; variable id
(s/def :dk.nqn.bandido-clojure/low :dk.nqn.bandido-clojure/uid)
(s/def :dk.nqn.bandido-clojure/hgh :dk.nqn.bandido-clojure/uid)

(s/def :dk.nqn.bandido-clojure/ite (s/coll-of int? :kind vector? :count 3))

(s/def :dk.nqn.bandido-clojure/t   (s/map-of int? :dk.nqn.bandido-clojure/ite)) ; tables
(s/def :dk.nqn.bandido-clojure/h   (s/map-of :dk.nqn.bandido-clojure/ite int?)) ; 
                                      
(s/fdef :dk.nqn.bandido-clojure/->Bdd
  :args (s/cat :dk.nqn.bandido-clojure/t :dk.nqn.bandido-clojure/uid))

(s/fdef :dk.nqn.bandido-clojure/->PartialResult
  (s/keys :req-un[:dk.nqn.bandido-clojure/t
                  :dk.nqn.bandido-clojure/uid
                  :dk.nqn.bandido-clojure/uid]))

