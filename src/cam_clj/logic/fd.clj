(ns cam-clj.logic.fd
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]))

;; Some of these examples are taken from https://github.com/clojure/core.logic/wiki/Features

(run* [q]
      (fd/in q (fd/interval 1 10)))

(run* [q]
      (fresh [x y]
             (fd/in x y (fd/interval 1 10))
             (fd/+ x y 10)
             (== q [x y])))

(run* [q]
      (fresh [x y]
             (fd/in x y (fd/interval 1 10))
             (fd/+ x y 10)
             (fd/distinct [x y])
             (== q [x y])))

(run* [q]
      (fresh [x y]
             (fd/in x y (fd/interval 1 10))
             (fd/+ x y 10)
             (fd/<= x y)
             (== q [x y])))

;; fd/eq is a macro that lets you write arithmetic expressions in normal
;; Lisp syntax
(run* [q]
      (fresh [x y]
             (fd/in x y (fd/interval 1 10))
             (fd/eq
              (= (+ x y) 9)
              (= (+ (* 2 x) (* 4 y)) 24))
             (== q [x y])))

;; Inefficient(?) permutations
(run* [q] (fresh [x y z]
                 (fd/in x y z (fd/domain 2 4 6))
                 (fd/distinct [x y z])
                 (== q [x y z])))

(time (dotimes [_ 1000000] (run* [q] (fresh [w x y z] (fd/in w x y z (fd/domain 2 4 6 8)) (fd/distinct [w x y z]) (== q [w x y z])))))

(time  (dotimes [_ 1000000] (run* [q] (permuteo [2 4 6 8] q))))
