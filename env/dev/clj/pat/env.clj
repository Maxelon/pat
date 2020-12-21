(ns pat.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [pat.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[pat started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[pat has shut down successfully]=-"))
   :middleware wrap-dev})
