
(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha15"]
                 [org.clojure/test.check "0.9.0"]
                 [adzerk/boot-test "1.2.0" :scope "test"]])

(require '[adzerk.boot-test :refer (test)])

(deftask testing []
  (merge-env! :source-paths #{"test"})
  identity)
