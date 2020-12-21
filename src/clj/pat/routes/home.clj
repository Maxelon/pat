(ns pat.routes.home
  (:require
    [pat.layout :as layout]
    [pat.model.import :as mi]
    [pat.middleware :as middleware]
    [ring.util.response]
    [ring.util.http-response :as response]))

(defn get-image [request]
  (response/ok (mi/get-image (:image (:path-params request)))))

(defn app-page [request]
  (layout/render request "pat.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get app-page}]
   ["/images/:image" {:get get-image}]])

