(ns system
  (:require [com.stuartsierra.component :as component]
            [reloaded.repl :refer[init start stop go reset]]
            [io.pedestal.http :as http]
            [parser.pedestal :as ped]
            [parser.routes :as rou]))

(defn new-system
  [env]
  (component/system-map
    :service-map
    {:env env
     ::http/routes rou/routes
     ::http/type :jetty
     ::http/port 8890
     ::http/join? false}

    :pedestal
    (component/using
      (ped/new-pedestal)
      [:service-map])))
