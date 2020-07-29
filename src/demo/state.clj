(ns demo.state)

(defn random-long []
  (rand-int Integer/MAX_VALUE))

(defn random-vector []
  (vec (take 100 (repeatedly random-long))))

(defn temp-file []
  (java.io.File/createTempFile "demo" ".tmp"))

;;;

(defprotocol IDispose
  (dispose! [_]))

(defprotocol IResource
  (-resource [_]))

(defrecord Service []
  IDispose
  (dispose! [_])
  IResource
  (-resource [_]
    (reify IDispose (dispose! [_]))))

(defn resource [_ svc]
  (-resource svc))

;;;

(defn payload []
  {:service (->Service), #_...})

(defn with-resource [{:keys [service] :as payload}]
  (assoc payload :resource (-resource service)))

(defn dispose-resource [{:keys [resource] :as payload}]
  (dispose! resource)
  (dissoc payload :resource))

(defn dispose-service [{:keys [service]}]
  ;; final lifecycle in the chain, return value is ignored
  (dispose! service))
