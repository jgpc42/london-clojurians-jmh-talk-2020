(comment

  (set! *warn-on-reflection* true)
  (set! *unchecked-math* :warn-on-boxed)

  (require 'bench)
  (run! (comp pprint dorun bench/run)
        [[:jmh]
         [:criterium]
         [:jmh :interleaved]
         [:criterium :interleaved]])

  (require '[no.disassemble :as dis])

  #_
  (pprint
   (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
       .getInputArguments
       seq))

  #_(prn *compiler-options*)

  (defn reset-atom [a]
    (swap! a (fnil inc 0)))

  (def a (atom nil))

  (reset-atom a)
  (assert (pos? @a))

  (print (dis/disassemble reset-atom))

  (require 'demo.core)
  (let [f @#'demo.core/value-at
        b (fn [] (f "a" 0))]
    (print (dis/disassemble b))
    [(b) (f [\a] 0)])

  (quit)

  )
