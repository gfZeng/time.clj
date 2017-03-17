
(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure    "1.9.0-alpha15" :scope "provided"]
                 [org.clojure/test.check "0.9.0"]

                 [adzerk/boot-test "1.2.0"  :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[adzerk.boot-test :refer (test)])

(deftask testing []
  (merge-env! :source-paths #{"test"})
  identity)



(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.2.0")
(bootlaces! +version+)


(task-options!
 push {:gpg-sign false}
 pom  {:project 'time
       :version +version+
       :url     "https://github.com/gfZeng/time.clj"
       :scm     {:url "https://github.com/gfZeng/time.clj"}
       :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})
