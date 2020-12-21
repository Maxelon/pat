(ns ^:dev/once pat.app
  (:require
    [pat.core :as core]
    [devtools.core :as devtools]))

(enable-console-print!)

(println "loading env/dev/cljs/spa/app.cljs...")

#_(devtools/install!)

(core/init!)
