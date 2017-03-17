(ns time.core-test
  (:require [time.core         :as t]
            [clojure.spec      :as s]
            [clojure.spec.gen  :as gen]
            [clojure.spec.test :as test]
            [clojure.string    :as str]))


(s/def ::offset (partial satisfies? t/IOffset))
(s/def ::time-zone (partial satisfies? java.util.TimeZone))

(s/fdef t/format
        :args (s/cat :fmt string? :date inst?)
        :ret  string?)

(s/fdef t/time-zone
        :args (s/cat :fmt
                     (s/? (s/and string? #(str/starts-with? % "GMT"))))
        :ret  ::time-zone)

