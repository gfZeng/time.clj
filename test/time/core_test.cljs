(ns time.core-test
  (:require [time.core :as t]))


(def m (js/require "moment"))

(println (re-find #"[^']+" "'年'"))
(time (dotimes [_ 1000]
        (re-find #"[^']+" "'年'")))

(time (dotimes [_ 1000]
        (first "yyyy")))

(println
 (t/format (t/date) "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'"))
(time
 (dotimes [_ 10000]
   (t/format (t/date) "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'")))

(println (.format (m) "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]"))
(time
 (dotimes [_ 10000]
   (.format (m) "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]")))


(println
 (t/format (t/date) "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'"))
(time
 (dotimes [_ 10000]
   (t/format (t/date) "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'")))

(println (.format (m) "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]"))
(time
 (dotimes [_ 10000]
   (.format (m) "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]")))
