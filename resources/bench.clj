(ns bench
  "You might want to pipe the edn data output of this script to a file,
  as it will generate quite a bit."
  (:require [criterium.core :as crit]
            [jmh.core :as jmh]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [demo.core :as demo]
            [demo.utils :as utils]
            [clojure.pprint :refer [pprint]])
  (:import [org.openjdk.jmh.util ScoreFormatter])
  (:gen-class))

#_(prn *compiler-options*)

(def bench-env
  (-> "benchmarks.edn" io/resource slurp edn/read-string))

(def bench-counts [31 100000])

(def bench-fns [[:str utils/make-str]
                [:vec utils/make-vec]])

(defmulti run identity)

(defn -main [& args]
  (-> (mapv edn/read-string args)
      run doall pprint))

;;;

(defn- protocol-compilation-info [f]
  (let [cache (.__methodImplCache demo/value-at)]
    #_(clojure.core/-reset-methods demo/ValueAt)
    (merge
     {:bench (class f)
      :cache {:mre (-> cache class (.getDeclaredField "mre")
                       (doto (.setAccessible true)) (.get cache))
              :map (.map cache)
              :table (seq (.table cache))}
      :value-at (class demo/value-at)}
     (into {} (for [[t {f :value-at}] (:impls demo/ValueAt)]
                [({clojure.lang.Indexed :vec, CharSequence :str} t)
                 (class f)])))))

(defn criterium-result [kind data]
  (let [data (-> (dissoc data :results) ;; discard fn return values (always \x)
                 (update :runtime-details dissoc :name))
        time (-> data :mean first double)
        [fact unit] (crit/scale-time time)
        score [(* (double fact) time), unit]
        fmt (apply format "%f %s" score)]
    {:run kind, :data data, :score score, :str fmt}))

(defn benchmark-fn [make count]
  (let [idx (* 0.5 count)
        val (make count)
        ^IFn value-at @#'demo/value-at]
    (fn [] (value-at val idx))))

(defn wrapper-object [name make count]
  (let [val (make count)]
    (if (= name :str)
      (demo/->Str val)
      (demo/->Idx val))))

;;;

(defn run-criterium [kind name count benchmark]
  (-> (criterium-result kind (crit/benchmark* benchmark {}))
      (assoc :tag [name count])))

(defn run-jmh [kind name count opts]
  (let [opts (merge {:fork {:count 1 :warmups 0}
                     ;; :profilers ["perfasm"]
                     ;; :status true, :verbose true
                     :output-time-unit :ns, :mode :average
                     :params {:count count}, :select name}
                    opts)
        ;; _ (prn opts)
        [data] (jmh/run bench-env opts)
        [n unit] (:score data)
        fmt (format "%s %s" (ScoreFormatter/format n) unit)]
    {:run kind, :tag [name count], :data data, :score [n unit], :str fmt}))

;;;

(defmethod run [:jmh] [kind]
  (for [[name] bench-fns
        count bench-counts]
    (run-jmh kind name count {})))

(defmethod run [:criterium] [kind]
  (for [[name make] bench-fns
        count bench-counts]
    (run-criterium kind name count (benchmark-fn make count))))

(defmethod run [:jmh :interleaved] [kind]
  (for [count bench-counts ;; swapped comprehension order
        [name] bench-fns]
    (run-jmh kind name count {})))

(defmethod run [:criterium :interleaved] [kind]
  (for [count bench-counts
        [name make] bench-fns]
    (run-criterium kind name count (benchmark-fn make count))))

(defmethod run [:criterium :interleaved :reversed] [kind]
  (for [count bench-counts
        [name make] (reverse bench-fns)]
    (run-criterium kind name count (benchmark-fn make count))))

;; NOTE: all of the following also are interleaved

(defmethod run [:criterium :round-robin] [kind]
  (->> (crit/benchmark-round-robin*
        (vec (for [count bench-counts
                   [name make] bench-fns]
               {:f (benchmark-fn make count)
                :expr-string (pr-str [name count])}))
        {})
       (map (partial criterium-result kind))))

(defmethod run [:jmh :non-forked] [kind]
  (for [count bench-counts
        [name] bench-fns]
    (run-jmh kind name count {:fork 0})))

(defmethod run [:jmh :interface] [kind]
  (for [count bench-counts
        [kname] bench-fns
        :let [opts {:select (keyword (str "i" (name kname)))}]]
    (run-jmh kind kname count opts)))

(defmethod run [:jmh :interface :non-forked] [kind]
  (for [count bench-counts
        [kname] bench-fns
        :let [opts {:fork 0, :select (keyword (str "i" (name kname)))}]]
    (run-jmh kind kname count opts)))

(defmethod run [:criterium :interface] [kind]
  (for [count bench-counts
        [name make] bench-fns
        :let [idx (* 0.5 count)
              obj (wrapper-object name make count)
              ^IFn value-at @#'demo/value-at-iface
              f (fn [] (value-at obj idx))]]
    (run-criterium kind name count f)))

(defmethod run [:jmh :protocol-interface] [kind]
  (for [count bench-counts
        [kname] bench-fns
        :let [opts {:select (keyword (str "p" (name kname)))}]]
    (run-jmh kind kname count opts)))

(defmethod run [:jmh :protocol-interface :non-forked] [kind]
  (for [count bench-counts
        [kname] bench-fns
        :let [opts {:fork 0, :select (keyword (str "p" (name kname)))}]]
    (run-jmh kind kname count opts)))

(defmethod run [:criterium :protocol-interface] [kind]
  (for [count bench-counts
        [name make] bench-fns
        :let [idx (* 0.5 count)
              obj (wrapper-object name make count)
              ^IFn value-at @#'demo/value-at
              f (fn [] (value-at obj idx))]]
    (run-criterium kind name count f)))
