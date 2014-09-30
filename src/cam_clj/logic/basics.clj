(ns cam-clj.logic.basics
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]))

;; You've got to run before you can walk

;; run* takes a list of goals and returns all the values of `q` for which
;; all of the goals succeed (if any).
(run* [q])

;; s# is a goal that succeeds
(run* [q] s#)

;; u# is a goal that always fails
(run* [q] u#)

;; == attempts to unify a logic variable with a value
(run* [q] (== q :foo))

;; *all* the goals must succeed if we are going to get any values back
(run* [q] (== q :foo) u#)

(run* [q] (== q :foo) s#)

;; The order of arguments to == doesn't matter
(run* [q] (== :foo q))

;; Once a logic variable has been unified with a value, it cannot be
;; unified with a different value
(run* [q]
      (== q :foo)
      (== q :bar))

;; So far we've seen goals that return only 1 value. But a goal may return many values.
(run* [q]
      (membero q [1 2 3]))

;; If we don't want all the values (or if the search would never terminate),
;; we use `run` to return just the first `n` matches:
(run 2 [q]
     (membero q (range 10)))

;; `conde` is like `cond`, but it evaluates every branch (think of it as "cond-every")
(run* [q]
  (conde
    [(== q :foo) s#]
    [(== q :bar) u#]
    [(== q :baz) s#]))

;; `fresh` is like `let`: it introduces a new logic variable, but without binding a value
(run* [q]
      (fresh [x]
             (== x 1)
             (== x q)))

;; A goal is just a Clojure function.

;; The First Commandment (from "The Reasoned Schemer")
;; "To convert a function whose value is a boolean into a function whose
;; value is a goal, replace `cond` with `conde` and unnest each question
;; and answer. Unnest the answer `true` by replacing it with `s#` and `false`
;; by replacing it with `u#`

(defn member?
  [x xs]
  (cond
   (empty? xs)      false
   (= x (first xs)) true
   :else            (member? x (rest xs))))

(member? 3 [1 2 3 4])
(member? 3 [1 2 4 5])

(defn my-membero
  [x xs]
  (conde
   [(emptyo xs) u#]
   [(firsto xs x) s#]
   [s# (fresh [d]
              (resto xs d)
              (my-membero x d))]))

(run* [q]
  (my-membero 3 [1 2 3 4]))

(run* [q]
  (my-membero 3 [1 2 4 5]))

(run* [q]
  (my-membero q [1 2 3]))

;; You might be wondering what this gives us over the standard `member?` function.
;; The cool thing is that `q` can appear in unexpected places.

(run* [q]
  (my-membero 4 [1 2 3 q]))

(run 3 [q]
  (my-membero :foo q))

;; For contrast, the Prolog version of `member` looks like this:
;;
;; member(X, [X|Tail])
;; member(X, [Head|Tail]) :- member(X,Tail)
;;
;; This is also a recursive definition, but is more succinct than our
;; core.logic implementaiton. We can take a first step at simplifying our
;; definition by deleting the `conde` clause that always fails:

(defn my-membero
  [x xs]
  (conde
   [(firsto xs x) s#]
   [s# (fresh [d]
              (resto xs d)
              (my-membero x d))]))

;; The similarity to the Prolog implementation is clear: we
;; have one branch where `x` is the head of the list, and a branch
;; that recurs to search for `x` in the tail.

;; We can go a step further by using core.logic's `defne`, which allows us
;; to define functions using matching:

(defne my-membero
  [x xs]
  ([_ [x . tail]])
  ([_ [head . tail]] (my-membero x tail)))

;; If you look at the core.logic source code, you'll see this is
;; exactly how `membero` is defined. Our new version of `my-membero` behaves just
;; like the original:

(run* [q] (my-membero q [1 2 3]))
(run 3 [q] (my-membero :foo q))

;; We have now met `run`, `fresh`, `conde`, and `==`. These are the primitives upon which
;; core.logic is built.
