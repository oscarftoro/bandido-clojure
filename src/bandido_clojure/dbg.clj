(ns bandido-clojure.dbg
  (:require
    [bandido-clojure.core :refer
     [init-table mk1 mk1a v low high apply' map->Bdd var* and* not*]]))

(defn -main [& args]
  (let [bdd      (init-table 3)
        bdd1     (var* 1 bdd)
        u1       (:uid bdd1)
        bdd2     (var* 2 bdd1)
        u2       (:uid bdd2)
        bdd3     (var* 3 bdd2)
        u3       (:uid bdd3)
        bdd4     (and* u1 u2 bdd3)]
   (println "quiubo mundi!")))





