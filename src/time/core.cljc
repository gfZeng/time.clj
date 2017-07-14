(ns time.core
  (:refer-clojure :exclude [> < >= <= == set get format])
  #?(:clj (:import [java.util Date TimeZone]
                   [java.text SimpleDateFormat]))
  (:require [clojure.string :as str]
            [clojure.pprint :refer [cl-format]]
            [clojure.core :as core]
            #?(:cljs [goog.string :as gstr])))


#?(:cljs
   (do (def Number js/Number)
       (def String js/String)
       (def Date   js/Date)
       (defrecord SimpleDateFormat [time-zone pattern ss pts regex])))

(defn str->num
  [s]
  (let [s (.trim s)]
    (cond
      (empty? s) 0
      (re-find #"\.\d" s) (#?(:clj Double/parseDouble  :cljs js/parseFloat) s)
      :else (#?(:clj Long/parseLong :cljs js/parseInt) s))))

(defprotocol Numberable
  (as-num [_]))

(extend-protocol Numberable
  nil
  (as-num [_] 0)
  Number
  (as-num [n] n)
  #?(:clj String :cljs string)
  (as-num [s] (str->num s)))

(defprotocol IDate
  (time-zone-offset  [this])
  (first-day-of-week [this])
  (as-date [this offset fdow]
    "offset = (time-zone-offset this) & fdow = (first-day-of-week this)"))

(defprotocol IFormat
  (-format [this ^Date   d])
  (-parse  [this ^String s]))

(defn set [d field val]
  (case field
    :year   #?(:clj  (.setYear d (- val 1900))
               :cljs (.setFullYear d val))
    :month  (.setMonth d (dec val))
    :date   (.setDate d val)
    :day    (.setDate d val)
    :hour   (.setHours d val)
    :minute (.setMinutes d val)
    :second (.setSeconds d val)
    :time   (.setTime d val))
  d)

(defn get [d field]
  (case field
    :year #?(:clj  (+ (.getYear d) 1900)
             :cljs (.getFullYear d))
    :month  (inc (.getMonth d))
    :date   (.getDate d)
    :day    (.getDate d)
    :hour   (.getHours d)
    :minute (.getMinutes d)
    :second (.getSeconds d)
    :time   (.getTime d)))

(defn date
  ([]  (Date.))
  ([d & {:keys [first-day-of-week time-zone]}]
   (let [offset (if time-zone
                  (time-zone-offset time-zone)
                  (time-zone-offset d))
         fdow   (if first-day-of-week
                  ({:sunday    0
                    :monday    1
                    :tuesday   2
                    :wednesday 3
                    :thursday  4
                    :friday    5
                    :saturday  6}
                   first-day-of-week
                   first-day-of-week)
                  (time.core/first-day-of-week d))]
     (as-date d offset fdow))))


#?(:cljs (declare formatter time-zone -format-num))

#?(:clj
   (do
     (extend-protocol IDate
       TimeZone
       (time-zone-offset [this]
         (.getRawOffset this)))

     (defn time-zone
       ([]    (TimeZone/getDefault))
       ([fmt]
        (if (and (string? fmt) (str/starts-with? fmt "GMT"))
          (TimeZone/getTimeZone fmt)
          (doto (time-zone)
            (.setRawOffset (time-zone-offset fmt))))))

     (def thread-local-formatter
       (memoize
        (fn formatter
          ([pattern]
           (thread-local-formatter (time-zone) pattern))
          ([tz pattern]
           (proxy [ThreadLocal] []
             (initialValue []
               (doto (SimpleDateFormat. pattern)
                 (.setTimeZone tz))))))))

     (defn formatter
       ([pattern]    (.get (thread-local-formatter pattern)))
       ([tz pattern] (.get (thread-local-formatter tz pattern))))

     (extend-type SimpleDateFormat
       IFormat
       (-format [this ^Date d]
         (.format this d))
       (-parse [this ^String s]
         (date (.parse this s)
               :time-zone (.getTimeZone this))))
     )

   :cljs
   (do
     (defn time-zone
       ([]    (time-zone (js/Date.)))
       ([fmt] (let [millis (time-zone-offset fmt)]
                (reify
                  IDate
                  (time-zone-offset [this] millis)
                  IHash
                  (-hash [this] millis)
                  IEquiv
                  (-equiv [this other]
                    (= millis (time-zone-offset other)))))))

     (defn -format-num [n x]
       (let [base (apply * (repeat n 10))]
         (cl-format nil (str "~" n ",'0d") (mod x base))))

     (defn- ->regex-digital [s]
       (str \(
            (if (= (first s) \')
              (gstr/regExpEscape (str/replace s #"^'|'$" ""))
              (apply str (repeat (count s) "\\d")))
            \)))

     (def formatter
       (memoize
        (fn formatter
          ([pattern]
           (formatter (time-zone) pattern))
          ([tz pattern]
           (assert (satisfies? IDate tz))
           (let [pts   (re-seq #"'[^']+'|y+|M+|d+|H+|m+|s+|S+" pattern)
                 ss    (str/split pattern #"'[^']+'|y+|M+|d+|H+|m+|s+|S+")
                 ss    (->> (repeat "")
                            (concat ss)
                            (take (count pts)))
                 regex (->> (map ->regex-digital pts)
                            (interleave ss)
                            (apply str)
                            (re-pattern))]
             (map->SimpleDateFormat {:time-zone tz
                                     :pattern   pattern
                                     :ss        ss
                                     :regex     regex
                                     :pts       pts}))))))

     (extend-type SimpleDateFormat
       IFormat
       (-format [this ^Date d]
         (let [d-offset (* 60000 (.getTimezoneOffset d))
               offset   (time-zone-offset (:time-zone this))
               d        (Date. (+ (.getTime d) offset d-offset))]
           (->> (map (fn [pt]
                       (case (first pt)
                         \y (-format-num (count pt) (get d :year))
                         \M (-format-num (count pt) (get d :month))
                         \d (-format-num (count pt) (get d :day))
                         \H (-format-num (count pt) (get d :hour))
                         \m (-format-num (count pt) (get d :minute))
                         \s (-format-num (count pt) (get d :second))
                         \S (-format-num (count pt) (get d :time))
                         \' (re-find #"[^']+" pt)
                         pt))
                     (:pts this))
                (interleave (:ss this))
                (apply str))))
       (-parse [this ^String s]
         (let [parsed (->> (re-find (:regex this) s)
                           (rest)
                           (zipmap (:pts this))
                           (reduce-kv (fn [m [k] v]
                                        (if (= k \')
                                          m
                                          (assoc m k (js/parseInt v))))
                                      {}))
               tz     (:time-zone this)
               ts     (-> (date)
                          (set :time   (parsed \S 0))
                          (set :year   (parsed \y 0))
                          (set :month  (parsed \M 0))
                          (set :day    (parsed \d 0))
                          (set :hour   (parsed \H 0))
                          (set :minute (parsed \m 0))
                          (set :second (parsed \s 0))
                          (.getTime))
               ts     (+ ts
                         (time-zone-offset (time-zone))
                         (- (time-zone-offset tz)))]
           (date ts :time-zone tz))))))

(extend-protocol IDate
  nil
  (time-zone-offset [_]
    (time-zone-offset (time-zone)))
  Number
  (time-zone-offset [this]
    (time-zone-offset (time-zone)))
  (first-day-of-week [this] 0)
  (as-date [this offset fdow]
    #?(:clj
       (proxy [Date time.core.IDate] [this]
         (time_zone_offset  [] offset)
         (first_day_of_week [] fdow)
         (as_date [offset fdow]
           (as-date (.getTime this) offset fdow)))

       :cljs
       (specify! (js/Date. this)
         IDate
         (time-zone-offset [this] offset)
         (first-day-of-week [this] fdow))))

  Date
  (time-zone-offset [this]
    (* -60000 (.getTimezoneOffset this)))
  (first-day-of-week [this]
    0)
  (as-date [this offset fdow]
    (as-date (.getTime this) offset fdow))

  #?(:clj String :cljs string)
  (time-zone-offset [this]
    (let [[op hours mins]
          (rest (re-find #"(?:GMT|Z)?([-+])?(\d+)?(?::(\d+))?$" this))

          op     ({"+" + "-" -} op +)
          millis (+ (* 3600 1000 (as-num hours))
                    (* 60000 (as-num mins)))]
      (op millis))))

(extend-type #?(:clj String :cljs string)
  IFormat
  (-format [this ^Date d]
    (let [^SimpleDateFormat fmt (formatter (time-zone d) this)]
      (-format fmt d)))
  (-parse [this ^String s]
    (-parse (formatter this) s)))

(defn format [d p]
  (-format p d))

(defn parse [s fmt]
  (-parse fmt s))

(defn copy [x field y]
  (set x field (get y field)))

(defn date-1970
  ([]   (date-1970 (time-zone)))
  ([tz] (Date. (- (time-zone-offset tz)))))

(defn floor
  [d period]
  (if (keyword? period)
    (floor d [1 period])
    (let [[n period] period
          begin      (date-1970 d)]
      (if (= period :week)
        (doto begin
          (copy :year d)
          (copy :month d)
          (.setDate  (- (.getDate d)
                        (mod (- (.getDay d)
                                (first-day-of-week d))
                             7))))
        (reduce
         (fn [begin field]
           (if (= field period)
             (reduced (set begin field (-> (get d field) (quot n) (* n))))
             (copy begin field d)))
         begin
         [:year :month :day :hour :minute :second])))))

(defn plus [^Date d period]
  (if (keyword? period)
    (plus d [1 period])
    (let [[n period] (if (#?(:clj identical? :cljs keyword-identical?) (second period) :week)
                       [(* 7 (first period)) :day]
                       period)]
      (doto (Date. (.getTime d))
        (set period (+ (get d period) n))))))

(defn floor-seq
  ([period] (floor-seq period (date)))
  ([period d]
   (iterate #(plus % period) (floor d period))))

(defn now-ms []
  #?(:clj  (System/currentTimeMillis)
     :cljs (.now js/Date)))

(defn > [d1 d2]
  (core/> (.getTime d1) (.getTime d2)))

(defn < [d1 d2]
  (core/< (.getTime d1) (.getTime d2)))

(defn >= [d1 d2]
  (core/>= (.getTime d1) (.getTime d2)))

(defn <= [d1 d2]
  (core/<= (.getTime d1) (.getTime d2)))

(defn == [d1 d2]
  (core/== (.getTime d1) (.getTime d2)))
