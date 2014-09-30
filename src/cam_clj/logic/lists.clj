(ns cam-clj.logic.lists
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]))

(defn concato [l1 l2 o]
  "A goal that succeeds if `o` is the concatenation of `l1` and `l2`"
  (conde
    ((== l1 ()) (== l2 o))
    ((fresh [a d r]
       (conso a d l1)
       (conso a r o)
       (appendo d l2 r)))))

;; This is a classic recursive solution: we make the problem smaller by splitting
;; the list `l1` into its head and tail (`car`, `a`, and `cdr`, `d`, in Scheme-speak),
;; and solve the smaller problem of appending the tail onto `l2`, giving the intermediate
;; result `r`. Consing together `a` and this intermediate result gives us the output `o`.

;; `q` can appear as any of the 3 arguments:
(run* [q] (concato [1 2 3] [4 5 6] q))

(run* [q] (concato [1 2 3] q [1 2 3 4 5 6]))

(run* [q] (concato q [4 5 6] [1 2 3 4 5 6]))

;; Perhaps it is clearer if we define it using matching:

(defne concato
  [l1 l2 o]
  ([() l2 l2])
  ([[x . tail] l2 [x . r]] (concato' tail l2 r)))

;; For comparison, here's a Prolog definition of conc:
;;
;; conc([],L,L).
;; conc([X|L1],L2,[X|L3]) :- conc(L1,L2,L3).

(defn inserto
  "A goal that succeeds if `l2` is the result of inserting `x` in `l1`"
  [x l1 l2]
  (conde
   [(conso x l1 l2)]
   [(fresh [a d r]
           (conso a d l1)
           (conso a r l2)
           (inserto x d r))]))

(run* [q] (inserto :a [1 2 3] q))
(run* [q] (inserto q [1 2 3] [1 2 :a 3]))
(run* [q] (inserto 1 q [2 1 3]))

(defn removeo
  "A goal that succeeds if `l2` is the result of removing `x` form `l2`"
  [x l1 l2]
  (inserto x l2 l1))

(run* [q] (removeo :a [:a :b :a :a] q))

(defn my-permuteo
  "A goal that succeeds if `xs` is a permutation of `ys`"
  [xs ys]
  (conde
   [(emptyo xs) (emptyo ys)]
   [s# (fresh [a d p]
              (conso a d xs)
              (permuteo d p)
              (inserto a p ys))]))

(run* [q] (my-permuteo [1 2 3] q))

(run* [q] (my-permuteo [] q))

(run 6 [q] (my-permuteo q [1 2 3]))

;; Does not terminate if `xs` is not ground
(run* [q] (my-permuteo q [1 2 3]))
