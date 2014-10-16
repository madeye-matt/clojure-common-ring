(ns com.madeye.clojure.common.ring
	(:require
      [clojure.data.codec.base64 :as b64]
      [ring.util.response :as response]
      [taoensso.timbre :as timbre]
    )
)

(timbre/refer-timbre)

(def media-type-error "application/vnd.com.madeye.error+json")

(defmulti get-boolean
  (fn [b] (class b))
)

(defmethod get-boolean java.lang.Boolean [b] b)
(defmethod get-boolean nil [b] false)
(defmethod get-boolean java.lang.String [b] (Boolean. b))

(defn base64-encode [original]
    (String. (b64/encode original) "UTF-8"))

(defn get-json-response
  ([status media-type response-map]
    (-> (response/response response-map)
        (response/content-type media-type)
        (response/status status)
    )
  )
  ([media-type response-map]
    (get-json-response 200 media-type response-map)
  )
)

(defn get-text-response
  [text]
  (-> (response/response text)
      (response/content-type "text/plain")
      (response/status 200)
  )
)

(defn- build-error-response
  ([status error-map]
    (-> (response/response error-map)
        (response/content-type media-type-error)
        (response/status status)
    )
  )
)

(def ^:const default-error-status 400)

(defn get-error-response
  ([status error error-description error-reason]
    (build-error-response status { :error error :error-description error-description :error-reason error-reason})
  )
  ([error error-description error-reason]
    (build-error-response default-error-status { :error error :error-description error-description :error-reason error-reason})
  )
  ([error error-description]
    (build-error-response default-error-status { :error error :error-description error-description})
  )
  ([ex]
    (let [error-status (or (:status (ex-data ex)) default-error-status)]
      (get-error-response error-status "invalid_request" (.getMessage ex) (:code (ex-data ex)))
    )
  )
)


(defn get-updatable-fields
  [token field-map]
  (let [roles (:roles token)
        all-fields (reduce clojure.set/union (map #(get field-map %) roles))]
    (debug "all-fields: " all-fields)
    all-fields
  )
)

(defn initialise
  [mte]
  (def media-type-error mte)
)
