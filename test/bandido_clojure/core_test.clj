(ns bandido-clojure.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop #?@(:cljs [:include-macros true])]
            [bandido-clojure.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))
