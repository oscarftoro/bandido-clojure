(ns bandido-clojure.core
  require [clojure.spec.alpha : as s])

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


;;specs
(s/def :dk.nqn.bandido-clojure/b #{0 1})
(s/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))
(s/def :dk.nqn.bandido-clojure/var (s/and int? #(> % 0)))
(s/def :dk.nqn.bandido-clojure/ite
  (s/keys :req-un []))
;; base fun(S/def :dk.nqn.bandido-clojure/uid (s/and int? #(> % 0)))ctions
(defn const[b] 1)
