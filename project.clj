(defproject time "0.4.4-SNAPSHOT"
  :description "time util for Clojure(Script)"
  :url "https://github.com/gfZeng/time.clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure    "1.9.0"]
                 [org.clojure/core.async "0.3.443"]]
  :repl-options {:init-ns time.core}
  :profiles {:dev {:dependencies [[commons-lang/commons-lang "2.2"]]}})
