(ns routing-example.user
  (:require [figwheel-sidecar.repl-api :as ra]))

(defn start []
  (ra/start-figwheel!)
  (ra/cljs-repl "dev"))

(defn stop []
  (ra/stop-figwheel!))
