(ns time.core-test
  (:require [time.core         :as t]
            [clojure.string    :as str]
            [clojure.test      :refer :all])
  (:import org.apache.commons.lang.time.DateUtils
           java.util.Calendar))


(deftest test-format
  (let [time-zone (t/time-zone)
        zero-zone (t/time-zone "+00:00")
        ts        (t/now-ms)
        fmt       "yyyy-MM-dd HH:mm:ss.S"
        offset    (- (t/time-zone-offset time-zone)
                     (t/time-zone-offset zero-zone))]
    (is (= (t/time-zone-offset zero-zone) 0))
    (is (= (t/format (t/date ts) fmt)
           (t/format
            (t/date (+ ts offset) :time-zone zero-zone)
            fmt)))))

(deftest test-plus
  (let [d (t/date)]
    (doseq [unit {:second Calendar/SECOND
                  :minute Calendar/MINUTE
                  :hour   Calendar/HOUR_OF_DAY
                  :day    Calendar/DAY_OF_MONTH
                  :week   Calendar/WEEK_OF_YEAR
                  :month  Calendar/MONTH
                  :year   Calendar/YEAR}
            :let [d (t/floor d (key unit))]]
      (doseq [i (map - (range 1 10000))]
        (is (= (t/plus d [i (key unit)])
               (DateUtils/add d (val unit) i))
            [i (key unit)])))))
