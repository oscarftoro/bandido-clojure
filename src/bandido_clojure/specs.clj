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


(defn var-num-in-t [pr var-num]
  "Ensure that the values i for 0 and 1 in :t corresponds to var-num.
   Remember that :t is a map u -> [i l h] where i is the variable number var-num, 
   l is a unique id of type u called low and
   h is a unique id of type u called high.
   When initialising the table, the values of 0 and 1 are equal to the maximal expected value 
   of i"
  (-> pr 
      (:t)
      (as-> ite 
          (let [[z _ _] (get ite 0)
                [o _ _] (get ite 1)]
            (and (= z o) (= var-num z))))))

(s/def ::partial-result (s/keys :req-un [::t
                                         ::uid
                                         ::uid]))

(s/fdef ::init-table
  :args (:var-num ::vid)
  :ret ::partial-result
  :fn #(var-num-in-t :ret :var-num))

(s/fdef ::->PartialResult
  :args (s/cat :t     ::t 
               :max-u ::uid 
               :u     ::uid)
  :ret  ::partial-result)





(s/def ::init-t (s/coll-of ::h :kind map? :count? 3 ))

(s/fdef ::partial-result->ht
  :args (:pr ::partial-result)
  :ret ::h )

