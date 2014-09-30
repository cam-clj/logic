(ns cam-clj.logic.flight-plan
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.pldb :refer [db-rel db]]
            [clojure.core.logic.arithmetic :as a]))

(db-rel flight flight-num from to dep arr days)

(def all-days [:mon :tue :wed :thur :fri :sat :sun])

(def timetable
  (db
   [flight "ba4733" "Edinburgh" "London" "09:40" "10:50" all-days]
   [flight "ba4773" "Edinburgh" "London" "13:40" "14:50" all-days]
   [flight "ba4833" "Edinburgh" "London" "19:40" "20:50" [:mon :tue :wed :thur :fri :sun]]
   [flight "ba4732" "London" "Edinburgh" "09:40" "10:50" all-days]
   [flight "ba4752" "London" "Edinburgh" "11:40" "12:50" all-days]
   [flight "ba4822" "London" "Edinburgh" "18:40" "19:50" [:mon :tue :wed :thur :fri]]
   [flight "ju201"  "London" "Ljubljana" "13:20" "16:20" [:fri]]
   [flight "ju213"  "London" "Ljubljana" "13:20" "16:20" [:sun]]
   [flight "ba614"  "London" "Zurich"    "09:10" "11:45" all-days]
   [flight "sr805"  "London" "Zurich"    "14:45" "17:20" all-days]
   [flight "ba510"  "London" "Milan"     "08:30" "11:20" all-days]
   [flight "az459"  "London" "Milan"     "11:00" "13:50" all-days]
   [flight "ju322"  "Ljubljana" "Zurich" "11:30" "12:40" [:tue :thur]]
   [flight "yu200"  "Ljubljana" "London" "11:10" "12:20" [:fri]]
   [flight "yu212"  "Ljubljana" "London" "11:25" "12:20" [:sun]]
   [flight "az458"  "Milan" "London"     "09:10" "10:00" all-days]
   [flight "ba511"  "Milan" "London"     "12:20" "13:10" all-days]
   [flight "sr621"  "Milan" "Zurich"     "09:25" "10:15" all-days]
   [flight "sr623"  "Milan" "Zurich"     "12:45" "13:35" all-days]
   [flight "yu323"  "Zurich" "Ljubljana" "13:30" "14:40" [:tue :thur]]
   [flight "ba613"  "Zurich" "London"    "09:00" "09:40" [:mon :tue :wed :thur :fri :sat]]
   [flight "sr806"  "Zurich" "London"    "16:10" "16:55" [:mon :tue :wed :thur :fri :sun]]
   [flight "sr620"  "Zurich" "Milan"     "07:55" "08:45" all-days]))

(defn flights-between
  ([from to]
     (run-db* timetable [q]
              (fresh [flight-num dep arr days]
                     (flight flight-num from to dep arr days)
                     (== q [flight-num dep arr days]))))
  ([from to day]
     (run-db* timetable[q]
              (fresh [flight-num dep arr days]
                     (flight flight-num from to dep arr days)
                     (membero day days)
                     (== q [flight-num dep arr])))))

(flights-between "London" "Edinburgh")

(flights-between "London" "Edinburgh" :sun)

(defn direct-flighto
  "A relation that succeeds if `route` is a direct flight between `from` and
  `to` on `day`."
  [from to day route]
  (fresh [flight-num dep arr days]
         (flight flight-num from to dep arr days)
         (membero day days)
         (== route [flight-num from to dep arr])))

(defn parse-time
  "Parse a time of the form `HH:MM` and return a vector of [hours minutes]"
  [t]
  (let [[_ h m] (re-find #"^(\d\d):(\d\d)$" t)]
    [(Long/parseLong h) (Long/parseLong m)]))

(defn duration-mins
  "Return the duration in minutes between `t1` and `t2`, which are assumed
   to be 24-hour clock times of the form `HH:MM`."
  [t1 t2]
  (let [[h1 m1] (parse-time t1)
        [h2 m2] (parse-time t2)]
    (+ (* 60 (- h2 h1))
       (- m2 m1))))

(defn transfero
  "A relation that succeeds if there is sufficient time to transfer from
   `flight1` to `flight2`, where a transfer may take up to 40 minutes."
  [flight1 flight2]
  (fresh [airport arr dep]
         (== flight1 [(lvar) (lvar) airport (lvar) arr])
         (== flight2 [(lvar) airport (lvar) dep (lvar)])
         (project [arr dep]
                  (a/>= (duration-mins arr dep) 40))))

(defn routeo
  "A goal that succeeds if route is a possible route between `from` and
  `to` on `day`."
  [from to day route]
  (conde
   [(fresh [flight]
           (direct-flighto from to day flight)
           (== route (list flight)))]
   [(fresh [intermediate first-leg other-legs]
           (direct-flighto from intermediate day first-leg)
           (routeo intermediate to day other-legs)
           (fresh [second-leg]
                  (firsto other-legs second-leg)
                  (transfero first-leg second-leg))
           (conso first-leg other-legs route))]))

;; How can I get from Milan to Edinburgh on a Monday?

(run-db 1 timetable [q] (routeo "Milan" "Edinburgh" :mon q))

;; I have to visit Milan, Ljubljana and Zurich, starting from London
;; on Tuesday and returning to London on Friday. In what sequence should
;; I visit these cities so that I have no more than one flight on each
;; day of the tour?

(run-db 1 timetable [q]
        (fresh [c1 c2 c3 r1 r2 r3 r4]
               (permuteo ["Milan" "Ljubljana" "Zurich"] [c1 c2 c3])
               (routeo "London" c1 :tue r1)
               (routeo c1 c2 :wed r2)
               (routeo c2 c3 :thur r3)
               (routeo c3 "London" :fri r4)
               (== q [r1 r2 r3 r4])))
