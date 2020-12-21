(defproject pat "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/data.zip "1.0.0"]
                 [com.draines/postal "2.0.3"]
                 [io.forward/clojure-mail "1.0.8"]
                 [com.fasterxml.jackson.core/jackson-core "2.11.2"]
                 [com.fasterxml.jackson.core/jackson-databind "2.11.2"]
                 ;
                 [cljs-ajax "0.8.0"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [reagent "0.10.0"]
                 [re-frame "1.0.0"]
                 [day8.re-frame/http-fx "0.2.1"]
                 [com.google.javascript/closure-compiler-unshaded "v20190618" :scope "provided"]
                 [org.clojure/google-closure-library "0.0-20190213-2033d5d9" :scope "provided"]
                 [thheller/shadow-cljs "2.8.39" :scope "provided"]
                 ;
                 [conman "0.9.0"]
                 [cprop "0.1.17"]
                 [expound "0.8.5"]
                 [funcool/struct "1.4.0"]
                 [luminus-migrations "0.6.7"]
                 [luminus-transit "0.1.2"]
                 [luminus-undertow "0.1.7"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.5"]
                 [metosin/jsonista "0.2.6"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.5"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.8.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.webjars.npm/bulma "0.9.0"]
                 [org.webjars.npm/material-icons "0.3.1"]
                 [org.webjars/webjars-locator "0.40"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [org.xerial/sqlite-jdbc "3.32.3"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.28"]]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot pat.core

  :plugins []

  :clean-targets ^{:protect false} [:target-path "target/cljsbuild" ".shadow-cljs"]


  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "pat.jar"
             :source-paths ["env/prod/clj" "env/prod/cljc" "env/prod/cljs"]
             :prep-tasks ["compile" ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn" ]
                  :dependencies [[binaryage/devtools "0.9.10"] ;; cljs-devtools
                                 [day8.re-frame/re-frame-10x "0.7.0"]
                                 [prone "2019-07-08"]
                                 [ring/ring-devel "1.8.0"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [jonase/eastwood "0.3.5"]] 
                  
                  :source-paths ["env/dev/clj" "env/dev/cljc" "env/dev/cljs"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user
                                 :timeout 120000}
                  }
   :project/test {:jvm-opts ["-Dconf=test-config.edn" ]
                  :resource-paths ["env/test/resources"] }
   :profiles/dev {}
   :profiles/test {}})
