(ns bandido-clojure.specs  
  (:require [clojure.spec.alpha :as s]))



(s/def ::b   #{0 1})                                      ; boolean value
(s/def ::uid (s/and int? #(> % 0)))                       ; unique id
(s/def ::vid (s/and int? #(>= % 1)))                      ; variable id is 1 or bigger (eq to ::uid)
(s/def ::low ::uid)
(s/def ::hgh ::uid)


(s/def ::ite (s/coll-of int? :kind vector? :count 3))

(s/def ::t   (s/map-of int? ::ite)) ; tables
(s/def ::h   (s/map-of ::ite int?)) 

(s/fdef ::->Bdd
  :args (s/cat ::t ::uid))

(s/def ::partial-result (s/keys :req-un [::t
                                         ::uid
                                         ::uid]))

(s/def ::init-t (s/coll-of ::h :kind map? :count? 3 ))



(s/fdef ::init-table
  :args (:var-num ::vid)
  :ret ::partial-result)

(s/fdef ::->PartialResult
  :args (s/cat :t     ::t 
               :max-u ::uid 
               :u     ::uid)
  :ret  ::partial-result)

(s/fdef ::partial-result->ht
  :args (:pr ::partial-result)
  :ret ::h )
