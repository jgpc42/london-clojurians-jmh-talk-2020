(ns demo.utils
  (:require [demo.core :as demo]))

(defn make-str [n]
  (apply str (repeat n \x)))

(defn make-vec [n]
  (vec (make-str n)))

(def make-wstr (comp demo/->Str make-str))
(def make-wvec (comp demo/->Idx make-vec))
