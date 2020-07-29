(ns demo.core
  (:import [java.util.concurrent TimeUnit]))

(defprotocol ValueAt
  (value-at [x idx]))

(extend-protocol ValueAt
  clojure.lang.Indexed
  (value-at [i idx]
    (.nth i idx))
  CharSequence
  (value-at [s idx]
    (.charAt s idx)))

;;;

(definterface IValueAt
  (valueAt [idx]))

(deftype Str [^CharSequence s]
  ValueAt
  (value-at [_ idx] (.charAt s idx))
  IValueAt
  (valueAt [_ idx] (.charAt s idx)))

(deftype Idx [^clojure.lang.Indexed i]
  ValueAt
  (value-at [_ idx] (.nth i idx))
  IValueAt
  (valueAt [_ idx] (.nth i idx)))

(defn value-at-iface [^IValueAt x idx]
  (.valueAt x idx))

;;;

(defn do-nothing [])

(defn add ^long [^long a ^long b]
  (unchecked-add a b))

(defn return-zero ^long []
  (unchecked-long 0))

;;;

(defn spin []
  (-> TimeUnit (. MILLISECONDS) (.sleep 100)))

(defn sum [xs]
  (apply + xs))

(let [inc-zero (fnil inc 0)]
  (defn reset-atom [a]
    (swap! a inc-zero)))

;;;

(defn hashcode [^ints arr]
  (java.util.Arrays/hashCode arr))

(defn hasheq [^ints arr]
  (hash arr))
