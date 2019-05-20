(ns bandido-clojure.core
  (:require [clojure.set :as set]
            [bandido-clojure.specs :as bspec]
            [clojure.spec.alpha :as s]
            [clojure.core.match :refer [match]]
            [clojure.java.shell :as shell]))




;;; ########################################################
;;; ###            Primitives ON 0 and 1                 ###
;;; ########################################################

(defn- bdd-not [b]
  (case (long b)
    1 0
    0 1))

(defn- bdd-or [b c]
  (case (long b)
    1 1
    0 (case (long c)
        1 1
        0 0)))

(defn- bdd-and [b c]
  (case (long b)
    0 0
    1 (bdd-or 0 c)))

;; not used
(defn- bdd-xor [b c]
  (let [d (bdd-and b (bdd-not c))
        e (bdd-and (bdd-not b) c)]
    (bdd-or d e)))

;;; ########################################################
;;; ###                BDDs FUNCTIONS'                   ###
;;; ########################################################

;;; naive implementation of ite
(defn ite [f g h]
  "Three argument operation that stands for If-then-else.
   When f is true returns g otherwise h.
   f is true if f and g is true and false when f is false and h is true"
  (bdd-or (bdd-and f g) (bdd-and (bdd-not f) h)))

(defn and' [f g]
  (ite f g 0))

(defn or' [f g]
  (ite f 1 g))

(defn xor' [f g]
  (ite f (bdd-not g) g))

(defn nor' [f g]
  (ite f 0 (bdd-not g)))

(defn xnor' [f g]
  (ite f g (bdd-not g)))

(defn not' [f]
  (ite f 0 1))

(defn nand' [f g]
  (ite f g 1))

(defn zero' [] 0)

(defn one' [] 1)

;;; ########################################################
;;; ###                 CONFIGURATION                    ###
;;; ########################################################

(comment
  "A BDD is a tuple (t, uid, luid) such that `t` is a map of type `uid -> [i l h]`
   and `uid` is an integer that stands for unique id and represents the current 
   computed result. 
   The value `luid` is the largest uid variable in table `t`.
   The tuple (t,uid,luid) is represented in this implementation as a record.x1x1
   The variable `i` represents the index of a variable x_i,
   for instance, variables are represented as x_1, x_2, x_3...x_n. Variable `l`
   represents the low branch whereas `h`, the high branch. Both are integers of
   type uid.")

(defrecord Bdd [t uid luid vars])

(comment
  "Bdds is a record that represents more than one boolean expression, the roots
   of those expressions are the uid elements of a vector of uids.")

(defrecord Bdds [t uids luid vars])

(defn init-table [var-num]
  "Initialise a record representing a node containing a table(map) u -> [ i l h].
   It returns a Bdd record where:
   `:t` is the unique table that represent a single, multirooted graph.
   `:uid` is the current result which is 1 at the begining.
 
   The algorithms are expecting to return different values of `:u` but since this is 
   is for the initialisation phase, the value of `:u` is nil"

  {:pre [(s/valid? ::bspec/vid var-num)]}
  (map->Bdd {:t    {0 [(inc var-num) 0 0] 1 [(inc var-num) 1 1]}
             :uid  1
             :luid 1
             :vars {}}))


(defn init-table2 [var-num]
  "Initialise a table that works with maps instead of records"
  {:t    {0 [(inc var-num) 0 0] 1 [(inc var-num) 1 1]}
   :uid  1
   :luid 1
   :vars {}})

;;; helper functions to extract values of ite vectors
;;; given a ite vector and a t table, returns the corresponding value

(defn v [u bdd] (-> ((:t bdd) u) first))
(defn low [u bdd] (-> ((:t bdd) u) second))
(defn high [u bdd] (-> ((:t bdd) u) (nth 2)))

(defn bdd->ht [bdd]
  "Takes a partial result and returns a map `h` which is the inverse of `t`"
  {:pre [(s/valid? ::bspec/bdd bdd)]}
  (clojure.set/map-invert (:t bdd)))

(defn bdd-map->ht [bdd-map]
  "Takes a map representing a bdd and returns a map `h`; the inverse of `t`"
  (clojure.set/map-invert (:t bdd-map)))


(defn- pack-operation-uid [operation vals bdd]              ; naming and placing this thing is challenging
  "returns a bdd containing the relevant data after applying an operation
  on u given some values and a context defined by a Bdd"
  (let [uid operation
        t1  (conj (:t bdd) [uid vals])]
    (map->Bdd
      {:t   t1
       :uid uid})))


(defn- update-uid [value bdd]                               ; same here
  "returns an updated Bdd containing a new :uid "
  (assoc bdd :uid value))

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
                  (assoc bdd :uid (ht [i l h]))   ; returns the saved value in h
                  (let [u  (-> bdd (:luid) (inc))
                        t1 (conj (:t bdd) [u [i l h]])
                        ovars (:vars bdd)
                        vars (merge ovars {i u})] ; this is our hack to mk
                    (map->Bdd
                      {:t    t1
                       :uid  u
                       :luid u
                       :vars vars}))))))

;;; mk using cond instead of if do not gain soo much
(defn mk1a [[i l h] bdd]
  (cond
    (= l h) bdd
    :else (let [ht (bdd->ht bdd)]
            (if (contains? ht [i l h])
              (assoc bdd :uid (ht [i l h]))
              (pack-operation-uid
                (-> bdd (:uid) (inc))
                [i l h]
                bdd)))))

;;; the simplest implementation use a map instead of a record

(defn mk2 [[i l h] m]
  "The essence of building reduced BDDs.
   Make a new entry representing a node in table t.
   A map result is returned"
  (if (= l h)
    m
    (let [ht (bdd-map->ht m)]
      (if (contains? ht [i l h])
        (assoc m :u (ht [i l h]))                           ; an updated map
        (let [u  (inc (:max-u m))
              t1 (conj (:t m) [u [i l h]])]
          {:t   t1
           :uid u})))))

;;; a mk3 could be implemented using the map metadata of the object to
;;; store var-num, this will require an init-table3 that uses with-meta 

;;; ########################################################
;;; ###                BUILD ALGORITHM                   ###
;;; ########################################################

;; potential design of an internal dsl for propositional logic
;; (p y q) entonces ((no r) o q)
;; (:and p q) :then ((:not r) :or q)
;; (:then (:and p q) ((:not r) :or q)
;; [:then [:and p q] [:or (:not r) q]]
;;

(def ops
  {'and and'
   'or or'
   'not not'})

(defmulti eval
          (fn [form] (:op form)))

(defmethod eval 'and
  [{:keys [op expr]}]
  (apply (ops op) (map eval expr)))

(defmethod eval 'not
  [])

(defn eval [t]
  "evaluates a logical formula t"
  true)
(defn expand
  "should return a  expanded t expression with variable x = b"
  [t x b] true) ;

(defn build' [t i n bdd]
  (if (> i n)
    (if (eval t) 1 0)
    (let [v0 'build0
          v1 'build1]
      (mk1 (i,v0,v1) bdd))))

(defn build [t n]
  (let [bdd init-table n]
    (build' t 1 n bdd)))

;;; ########################################################
;;; ###                APPLY ALGORITHM                   ###
;;; ########################################################


(defn- eval-op
  [op u1 u2]
  (case op
    :and (and' u1 u2)
    :or (or' u1 u2)
    :xor (xor' u1 u2)))

(defn- memo [f]
  (let [mem (atom {})]
    (fn [& args]

      (if-let [e (find @mem [(nth args 1) (nth args 2)])]
        (->> (nth args 3)
             (update-uid (val e)))                          ; (val e) is the uid saved in g
        (let [bdd (apply f args)
              uid (:uid bdd)
              u1  (nth args 1)
              u2  (nth args 2)]
          (swap! mem assoc [u1 u2] uid)
          bdd)))))

(defn- app [op u1 u2 bdd]
  (cond
    (-> (contains? #{0 1} u1)
        (and (contains? #{0 1} u2))) (update-uid (eval-op op u1 u2) bdd)
    (= (v u1 bdd) (v u2 bdd)) (let [lbdd (app op (low u1 bdd) (low u2 bdd) bdd)
                                    low  (:uid lbdd)
                                    hbdd (app op (high u1 bdd) (high u2 bdd) lbdd)
                                    high (:uid hbdd)]
                                (mk1 [(v u1 bdd) low high] hbdd))

    (< (v u1 bdd) (v u2 bdd)) (let [lbdd (app op (low u1 bdd) u2 bdd)
                                    low  (:uid lbdd)
                                    hbdd (app op (high u1 bdd) u2 lbdd)
                                    high (:uid hbdd)]
                                (mk1 [(v u1 bdd) low high] hbdd))

    :else (let [lbdd (app op u1 (low u2 bdd) bdd)
                low  (:uid lbdd)
                hbdd (app op u1 (high u2 bdd) lbdd)
                high (:uid hbdd)]
            (mk1 [(v u2 bdd) low high] hbdd))))


(def apply' (memo app))

;;; ######################################################## 
;;; ###                 BASE FUNCTIONS                   ###
;;; ########################################################

(defn const [b]
  "const takes 1 or 0 and returns the parameter"
  {:pre [(s/valid? ::bspec/b b)]} b)

;; TODO: var(i) = x_i

(defn var* [i bdd]
  (let [result-bdd (mk1 [i 0 1] bdd)
        uid        (:uid result-bdd)
        vars       (:vars result-bdd)]
    (assoc result-bdd :vars (merge vars {i uid}))))



;;; ######################################################## 
;;; ###              ALGEBRAIC OPERATIONS*               ###
;;; ########################################################

(defn id-t->max-uid [id t]
  "Given an id and a t table it returns the largest uid value corresponding to
   the variable id"
  (let [uid-inf-vec (->> t
                         (apply (into hash-set))
                         reverse
                         (some (fn [[u [i l h]]] (if (= i  id) [u [i l h]]))))
        [uid _] uid-inf-vec]
    uid))


(defn and*
  "takes variable numbers as inputs"
  [f g bdd] (let [vars        (:vars bdd)
                  u1 (get vars f)
                  u2 (get vars g)] (apply' :and u1 u2 bdd)))

(defn and** "takes uids as inputs"
  [f g bdd] (apply' :and f g bdd))

(defn or* [f g bdd] (apply' :or f g bdd))

(defn xor* [f g bdd] (apply' :xor f g bdd))

;; i think the semantics of not are wrong here.
;; when a search finds one value of i we should take that inf value
;; invert the hi and lo branches and return the u
;; if a not is already defined we should return the u corresponding
;; to the not
(defn not*
  "not takes a variable id and negates all those variables"
  [f bdd] (let [t    (->> bdd :t)
                nots (reduce-kv (fn [m k [i l h :as inf]]   ; build a new t with all var i negated
                                  (if (= i f)
                                    (assoc m k [i h l])     ;swap l h
                                    (assoc m k inf)))       ;do nothing
                                {}
                                t)]
            (assoc bdd :t nots)))

(defn not**
  "takes a uid and negate its inf"
  [u bdd] (let [inf    (-> bdd :t (get u))
                [i l h] inf
                new-t   (assoc (:t bdd) u [i h l])]
            (assoc bdd :t new-t)))
(defn not2
  "takes an f and a bdd, finds the value inf = [i h l] s.t. i = f
   and make a new row not-inf= [i l h] in the t table. returns the
   updated bdd"
  [f bdd]

  (let [t (->> bdd :t)] t))
  



;;; ######################################################## 
;;; ###                 PLOT OPERATIONS                  ###
;;; ########################################################


(defn- u-inf-dot->dot-line [u inf dot]
  (match inf
         [i 0 0] (str dot "graph { 1 [shape=box] 0 [shape=box] ")
         [i 1 1] dot
         [i l h] (let [labels
                           (str dot
                                (format
                                  " %d [label=<X<SUB>%d</SUB>>,shape=circle, xlabel=%d] "
                                  u i u))
                       low (format " %d -- %d [style=dashed]" u l)
                       hgh (format " %d -- %d " u h)]
                   (str labels low hgh))))

(defn bdd->dot [bdd]
  (let [arr (->
              bdd
              :t (->> (sort-by first <)))
        beg (reduce (fn [dot [i inf :as el]]
                      (u-inf-dot->dot-line i inf dot)) "" arr)]
    (str beg "}")))

(defn bdd->file! [bdd]
  (let [dot       (bdd->dot bdd)
        file-name "output/bdd.dot"]
    (clojure.java.io/make-parents file-name)
    (spit file-name
          dot)))

(defn bdd->svg! [bdd]
  (do
    (bdd->file! bdd)
    (shell/sh "dot" "-Tsvg" "output/bdd.dot" "-o" "output/bdd.svg")))
