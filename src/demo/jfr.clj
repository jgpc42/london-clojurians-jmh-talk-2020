(ns demo.jfr
  (:import
   [org.openjdk.jmh.results
    Aggregator AggregationPolicy
    Result ResultRole])
  (:gen-class
   :name demo.jfr.ExampleProfiler
   :implements [org.openjdk.jmh.profile.ExternalProfiler]
   :prefix "--"
   :main false))

(declare ->result)

(defn --getDescription [_]
  "Run Java Flight Recorder for each benchmark.")

(defn --allowPrintOut [_]
  true)

(defn --allowPrintErr [_]
  false)

(defn --addJVMInvokeOptions [_ params]
  [])

(defn --addJVMOptions [_ params]
  ;; https://docs.oracle.com/en/java/javase/13/docs/specs/man/java.html
  (let [file (str (System/currentTimeMillis) ".jfr")
        opts ["dumponexit=true"
              "settings=profile"
              (str "filename=" file)]]
    [(apply str "-XX:StartFlightRecording="
            (interpose "," opts))]))

(defn --beforeTrial [_ params])

(defn --afterTrial [_ bench-result pid stdout stderr]
  [(->result
    (pr-str
     {:file (->> bench-result .getParams .getJvmArgs
                 (some #(when (.contains % ",filename=")
                          (second (.split % ",filename=")))))
      :result (str bench-result)
      :pid pid
      :stdout (.getPath stdout)
      :stderr (.getPath stderr)}))])

;;;

(defn aggregator []
  (proxy [Aggregator] []
    (aggregate [results]
      (->result (apply str results)))))

(defn ->result [message]
  (let [stats (-> Result ;; for some reason, Result/of is protected
                  (.getDeclaredMethod "of" (into-array Class [Double/TYPE]))
                  (doto (.setAccessible true))
                  (.invoke nil (into-array Object [0.0])))]
    (proxy
        [Result]
        [ResultRole/SECONDARY "JFR" stats "N/A" AggregationPolicy/SUM]
        (getThreadAggregator [] (aggregator))
        (getIterationAggregator [] (aggregator))
        (toString [] message))))
