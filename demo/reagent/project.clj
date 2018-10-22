(defproject routing-example "0.1.0-SNAPSHOT"
  :description "Client side routing with bidi, accountant and clerk"
  :url "https://github.com/PEZ/routing-example"

  :min-lein-version "2.5.3"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 #_[org.clojure/core.async "0.3.443"
                    :exclusions [org.clojure/tools.reader]]
                 [org.clojure/core.async "0.4.474"]
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.1"]
                 #_[pez/clerk "1.0.0-SNAPSHOT"] ;; this demo uses :source-paths instead since it lives in the clerk repo
                 [bidi "2.1.4"]
                 [venantius/accountant "0.2.4"]]

  :plugins [[lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src" "../../src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[prismatic/schema "1.1.7"]]}
             :repl {:plugins [[cider/cider-nrepl "0.18.0"]]
                    :dependencies [[nrepl "0.4.5"]
                                   [cider/piggieback "0.3.9"]
                                   [figwheel-sidecar "0.5.16"]]}}


  :cljsbuild {:builds
              {:dev
               {:source-paths ["src"]

                :figwheel {:on-jsload "routing-example.core/on-js-reload"
                           :websocket-host :js-client-host}


                :compiler {:main routing-example.core
                           :asset-path "/js/compiled/out"
                           :output-to "resources/public/js/compiled/routing_example.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               :min
               {:source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/routing_example.js"
                           :main routing-example.core
                           :optimizations :advanced
                           :pretty-print false}}}}

  :figwheel {:http-server-root "public"
             :server-port 4449
             :server-ip "0.0.0.0"
             :css-dirs ["resources/public/css"]
             :ring-handler routing-example.server/handler}

  :repl-options {:init-ns routing-example.user
                 :skip-default-init false
                 :nrepl-middleware [cider.piggieback/wrap-cljs-repl]})
