(ns cam-clj.logic.n-queens
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]))

(defn solutiono
  "A goal that succeeds if `positions` is a valid arrangement of queens
   on an `n` x `n` board. This implementation depends on the observation
   that, if [x,y] and [x',y'] are on the same diagonal, then x+y == x'+y' (if
   the diagonal has a negative gradient) or x-y == x'-y' (if the diagonal has
   a positive gradient). If the `n` queens do not attack each other, then the
   x-coordinates (xs below), the y-coordinates (ys), negative diagonals (us),
   and positive diagonals (vs) must be distinct."
  [n positions]
  (let [xs (vec (repeatedly n lvar))
        ys (vec (repeatedly n lvar))
        us (vec (repeatedly n lvar))
        vs (vec (repeatedly n lvar))]
    (all
     (== xs (range n)) ; wlog
     (everyg (fn [i]
               (all
                (fd/in (ys i) (fd/interval 0 (- n 1)))
                (fd/in (us i) (fd/interval 0 (- (* 2 n) 1)))
                (fd/in (vs i) (fd/interval (- 1 n) (- n 1)))
                (fd/+ (xs i) (ys i) (us i))
                (fd/- (xs i) (ys i) (vs i))))
             (range n))
     (fd/distinct ys)
     (fd/distinct us)
     (fd/distinct vs)
     (== positions (map vector xs ys)))))
