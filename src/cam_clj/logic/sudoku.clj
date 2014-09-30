(ns cam-clj.logic.sudoku
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]))

;;  0 1  2  3  4  5  6  7  8
;;  9 10 11 12 13 14 15 16 17
;; 18 19 20 21 22 23 24 25 26
;; 27 29 29 30 31 32 33 34 35
;; 36 37 38 39 40 41 42 43 44
;; 45 46 47 48 49 50 51 52 53
;; 54 55 56 57 58 59 60 61 62
;; 63 64 65 66 67 67 69 70 71
;; 72 73 74 75 76 77 78 79 80

(defn row
  "Indices of row `n` (0 <= n < 9)"
  [n]
  (for [c (range 9)] (+ (* 9 n) c)))

(defn col
  "Indices of column `n` (0 <= n < 9)"
  [n]
  (for [r (range 9)] (+ (* 9 r) n)))

(defn square
  "Indices of minor square `n` (0 <= n < 9)"
  [n]
  (let [top-left (+ (* 27 (quot n 3))
                    (*  3 (mod  n 3)))]
    (for [r (range 3) c (range 3)]
      (+ top-left (* 9 r) c))))

(defn parse-grid
  "Grid should be a string of 81 characters. Returns a vector of corresponding integer
  (when the character at that position in the string is a digit 1-9) otherwise a new
  lvar."
  [grid]
  (let [digit? (set "123456789")]
    (reduce (fn [accum v]
              (if (digit? v)
                (conj accum (Integer/parseInt (str v)))
                (conj accum (lvar))))
            []
            (seq grid))))

(defn select
  [xs selector]
  (fn [index]
    (map xs (selector index))))

(defn solve
  [grid]
  (run 1 [q]
        (let [xs (parse-grid grid)]
          (all
           (everyg (fn [x] (fd/in x (fd/domain 1 2 3 4 5 6 7 8 9))) xs)
           (everyg fd/distinct (map (select xs row) (range 9)))
           (everyg fd/distinct (map (select xs col) (range 9)))
           (everyg fd/distinct (map (select xs square) (range 9)))
           (== q xs)))))

(def grid1 "530070000600195000098000060800060003400803001700020006060000280000419005000080079")

(def grid2 "008601000600000003000048506040000600780020091001000030109870000200000007000209100")
