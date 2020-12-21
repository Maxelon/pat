(ns pat.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[pat started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[pat has shut down successfully]=-"))
   :middleware identity})
