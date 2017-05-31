
(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure    "1.9.0-alpha16" :scope "provided"]
                 [adzerk/boot-test "1.2.0"  :scope "test"]
                 [commons-lang/commons-lang "2.2" :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[adzerk.boot-test :refer (test)]
         '[adzerk.bootlaces :refer :all])

(def +version+ "0.2.5")

(bootlaces! +version+)

(task-options!
 pom  {:project 'time
       :version +version+
       :url     "https://github.com/gfZeng/time.clj"
       :scm     {:url "https://github.com/gfZeng/time.clj"}
       :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})
