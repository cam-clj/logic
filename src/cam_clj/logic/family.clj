(ns cam-clj.logic.family
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.pldb :refer [with-db db-rel db db-facts]]))

;; Note that we've imported some symbols from clojure.core.logic.pldb - these allow
;; us to work with in-memory databases of facts (or relations).

;; Start by defining some basic relations
(db-rel parento parent child)
(db-rel femaleo person)
(db-rel maleo person)

;; Using these relations, define a well-known family tree
(def facts
  (db
   [parento "Abraham" "Herb"]
   [parento "Abraham" "Homer"]
   [parento "Mona"    "Herb"]
   [parento "Mona"    "Homer"]
   [parento "Clancy"  "Marge"]
   [parento "Clancy"  "Patty"]
   [parento "Clancy"  "Selma"]
   [parento "Jackie"  "Marge"]
   [parento "Jackie"  "Patty"]
   [parento "Jackie"  "Selma"]
   [parento "Homer"   "Bart"]
   [parento "Homer"   "Lisa"]
   [parento "Homer"   "Maggie"]
   [parento "Marge"   "Bart"]
   [parento "Marge"   "Lisa"]
   [parento "Marge"   "Maggie"]
   [parento "Selma"   "Ling"]
   [maleo "Abraham"]
   [maleo "Herb"]
   [maleo "Homer"]
   [femaleo "Mona"]
   [maleo "Clancy"]
   [femaleo "Marge"]
   [femaleo "Patty"]
   [femaleo "Selma"]
   [femaleo "Jackie"]
   [maleo "Bart"]
   [femaleo "Lisa"]
   [femaleo "Maggie"]
   [maleo "Ling"]))

;; We can use our database of facts to answer questions...

;; Who are Abraham's children?

(with-db facts
  (run* [q]
        (parento "Abraham" q)))

;; Who are Bart's parents?

(with-db facts
  (run* [q]
        (parento q "Bart")))

;; We're going to be typing `(with-db ... (run* ...))` quite a lot, so core.logic gives
;; us a handy short-cut:

(run-db* facts [q] (parento q "Bart"))

;; We can define new relations in terms of parento, maleo, and femaleo

(defn fathero
  "A relation that succeeds if `x` is the father of `y`"
  [x y]
  (all
   (parento x y)
   (maleo x)))

(defn mothero
  "A relation that succeeds if `x` xs the mother of `y`"
  [x y]
  (all
   (parento x y)
   (femaleo x)))

;; Who is the mother of Lisa?

(run-db* facts [q] (mothero q "Lisa"))

;; Grandparent is slightly more tricky

(defn grandparento
  "A relation that succeeds if `x` is a grandparent of `y`"
  [x y]
  (fresh [z]
         (parento x z)
         (parento z y)))

;; Who are Lisa's grandparents?

(run-db* facts [q] (grandparento q "Lisa"))

;; We can use the grandparento relation to find the grandchildren too

(run-db* facts [q] (grandparento "Clancy" q))

;; Brother and sister...

(defn siblingo
  "A relation that succeeds if `x` is a full sibling of `y`"
  [x y]
  (fresh [ma pa]
         (mothero ma x)
         (fathero pa x)
         (mothero ma y)
         (fathero pa y)
         (!= x y)))

(run-db* facts [q] (siblingo q "Lisa"))

(defn brothero
  "A relation that succeeds if `x` is a brother of `y`"
  [x y]
  (all
   (siblingo x y)
   (maleo x)))

(defn sistero
  "A relation that succeeds if `x` is a sister of `y`"
  [x y]
  (all
   (siblingo x y)
   (femaleo x)))

(run-db* facts [q] (brothero q "Lisa"))

(run-db* facts [q] (sistero q "Lisa"))

(run-db* facts [q] (sistero "Lisa" q))

;; Bart is not the sister of anyone
(run-db* facts [q] (sistero "Bart" q))

;; We can also define recursive goals:

(defn ancestero
  "A goal that succeeds if `x` is an ancestor of `y`"
  [x y]
  (conde
   [(parento x y) s#]
   [s# (fresh [z]
              (parento z y)
              (ancestero x z))]))

(run-db* facts [q] (ancestero q "Homer"))

(run-db* facts [q] (ancestoro q "Bart"))

;; We can imagine a world in which our family is not frozen in time:

(run-db* (db-facts facts
                   [parento "Bart" "Bart's Elder Son"]
                   [parento "Jenda" "Bart's Elder Son"]
                   [maleo "Bart's Elder Son"])
         [q]
         (ancestero q "Bart's Elder Son"))

;; Thanks to Clojure's persistent data structures, our facts have not changed:

(run-db* facts [q] (parento "Bart" q))

;; Finally, we can define `descendanto` in terms of `ancestero`

(defn descendanto
  "A goal that succeeds if `x` is a descendant of `y`"
  [x y]
  (ancestero y x))

(run-db* facts [q] (descendanto q "Clancy"))
