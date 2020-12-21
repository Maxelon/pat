(ns pat.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.exception :as exception]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.ring.middleware.multipart :as multipart]
    [pat.db.core :as db]
    [pat.model.import :as i]
    [pat.model.schedule :as sc]
    [pat.model.mail :as m]
    [pat.middleware :as middleware]
    [ring.util.http-response :as response]
    [pat.middleware.formats :as formats]))

(defn assets-api [request]
  (response/ok {:assets (db/get-assets)}))

(defn reference-api [request]
  (response/ok {:reference {:batches (db/batches)
                            :locations (db/list-locations)
                            :schedule (sc/schedule)}}))

(defn imports-api [request]
  (let [_ (future (m/sync-inbox!))]
    (response/ok {:imports {:batches (i/get-batches) :results (db/missing-links)}})))

(defn update-asset!
  [asset]
  (let [results (:results asset)
        orphans (filter #(not (:aid %)) results)
        asset-results (filter :aid results)]
    (doseq [r orphans] (db/orphan! r))
    (if (empty? asset-results)
      (db/delete-stub! asset)
      (do
        (db/update-assessment! (first asset-results))
        (db/set-asset! asset)))))

(defn link-result!
  [result]
  (if (:aid result)
    (db/update-link! result)
    (db/add-asset! result)))

(defn service-routes []
  ["/api"
   {:middleware [parameters/parameters-middleware           ; query-params & form-params
                 muuntaja/format-negotiate-middleware       ; content negotiation
                 muuntaja/format-response-middleware        ; encoding response body
                 exception/exception-middleware             ; exception handling
                 muuntaja/format-request-middleware         ; decoding request body
                 coercion/coerce-response-middleware        ; coercing response body
                 coercion/coerce-request-middleware         ; coercing request parameters
                 multipart/multipart-middleware]
    :muuntaja   formats/instance
    :coercion   spec-coercion/coercion
    :swagger    {:id ::api}}
   ["" {:no-doc true}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]
    ["/swagger-ui*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"})}]]
   ["/assets"
    {:get
     {:responses
                  {200
                   {:body
                    {:assets
                     [{:aid         pos-int?
                       :description string?}]}}}
      :handler    assets-api
      :parameters {}}}]
   ["/reference"
    {:get
     {:responses
                  {200
                   {:body
                    {:reference
                     {}}}}
      :handler    reference-api
      :parameters {}}}]

   ["/imports"
    {:get
     {:responses
                  {200
                   {:body
                    {:imports {}}}}
      :handler    imports-api
      :parameters {}}}]
   ["/import"
    {:post
     (fn [request]
       (try
         (println ":body-params > " (:body-params request))
         (let [batch (:body-params request)]
           (if (:accept batch)
             (i/import-batch batch)
             (i/skip-batch batch)))
         ;; DO STUFF
         (imports-api {})
         #_(response/ok {:status :ok})
         (catch Exception e
           (let [{id     :pat/error-id
                  errors :errors} (ex-data e)]
             (response/internal-server-error
               {:errors {:server-error ["Failed to save batch"]}})))))}]
   ["/asset"
    {:post
     (fn [request]
       (try
         (update-asset! (:body-params request))
         (response/ok {:status :ok})
         (catch Exception e
           (let [{id     :pat/error-id
                  errors :errors} (ex-data e)]
             (response/internal-server-error
               {:errors {:server-error ["Failed to save asset"]}})))))}]
   ["/link"
    {:post
     (fn [request]
       (try
         (link-result! (:body-params request))
         (response/ok {:status :ok})
         (catch Exception e
           (let [{id     :pat/error-id
                  errors :errors} (ex-data e)]
             (response/internal-server-error
               {:errors {:server-error ["Failed to save asset"]}})))))}]])
