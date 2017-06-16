(ns time.chime
  (:require [time.core :as t]
            #?(:cljs [cljs.core.async    :as a :refer [timeout chan]]
               :clj  [clojure.core.async :as a :refer [timeout chan]])))


(defn chime-ch* [periods ch]
  (if-let [p (first periods)]
    (a/take! (timeout (- (.getTime p) (t/now-ms)))
             (fn [_]
               (when (a/put! ch p)
                 (chime-ch* (rest periods) ch))))
    (a/close! ch)))

(defn chime-ch
  ([periods]    (chime-ch periods (chan)))
  ([periods ch] (let [now (t/date)]
                  (chime-ch* (drop-while #(t/<= % now) periods) ch)
                  ch)))

(defn cron [ch f]
  (a/take! ch (fn [p]
                (when p
                  (f)
                  (cron ch f)))))

(defmacro with-chime [[ch expr] & body]
  `(let [~ch ~expr]
     (cron ~ch (fn [] ~@body))))
