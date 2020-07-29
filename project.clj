(def dependencies
  (->> "deps.edn" slurp read-string
       :deps (mapv #(vector (% 0) (:mvn/version (% 1))))))

(def agent-jar
  (as-> (clojure.java.io/file
         (System/getProperty "user.home")
         ".m2" "repository"
         "nodisassemble" "nodisassemble" "0.1.3"
         "nodisassemble-0.1.3.jar") f
    (and (.exists f) (.getCanonicalPath f))))

(defproject london-clojurians-jmh-talk-2020 "0.1.0-SNAPSHOT"
  :url "https://github.com/jgpc42/london-clojurians-jmh-talk-2020"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies ~dependencies

  :aot [demo.jfr]
  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :aliases
  {"bench" ["run" "-m" "bench"]
   "bench-all" ["do"
                "bench" ":jmh,"
                "bench" ":criterium,"
                "bench" ":jmh" ":interleaved,"
                "bench" ":criterium" ":interleaved,"
                "bench" ":criterium" ":interleaved" ":reversed"]
   "result" ["run" "-m" "result"]}

  :profiles
  {:dev
   {:plugins [[lein-jmh "0.2.8"]]}

   :jitwatch
   {:aot [demo.core, demo.utils, demo.state, bench]
    :main bench
    :target-path "target/jitwatch/"
    :uberjar-name "jitwatch.jar"}

   :repl
   {:dependencies [[nodisassemble "0.1.3"]]
    :jvm-opts ~`["-Dclojure.compiler.disable-locals-clearing=true"
                 ~@(when agent-jar
                     [(str "-javaagent:" agent-jar)])]
    :source-paths ["dev"]}})
