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


(println "~~~~~~~~~~~~~~~~ parse benchmark ~~~~~~~~~~~~~~~~")
(println
 (let [s (.format (m) "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]")]
   (m s "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]")))
(time
 (let [s (.format (m) "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]")]
   (dotimes [_ 10000]
     (m s "YYYY[年]MM[月]DD[日] HH[时]mm[分]ss[秒]"))))
(time
 (dotimes [_ 10000]
   (m "2017-07-17T16:29:33")))

(println
 (let [s (t/format (t/date) "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'")]
   (t/parse s "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'")))
(time
 (let [s (t/format (t/date) "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'")]
   (dotimes [i 10000]
     (t/parse s "yyyy'年'MM'月'dd'日' HH'时'mm'分'ss'秒'"))))
(time
 (let [s (t/format (t/date) "yyyy-MM-dd'T'HH-mm-ss")]
   (dotimes [i 10000]
     (t/parse s "yyyy-MM-dd'T'HH-mm-ss"))))
