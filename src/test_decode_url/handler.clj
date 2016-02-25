(ns test-decode-url.handler
  (:import
   [io.undertow Undertow UndertowOptions])
  (:require
   [compojure.core :refer :all]
   [compojure.route :as route]
   [immutant.web :as web]
   [immutant.web.undertow :refer [options]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/:param" [param] (str "Your param: " param))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn start-web
  []
  (let [^Undertow$Builder builder
        (doto (Undertow/builder) (.setServerOption UndertowOptions/DECODE_URL false))]
    #_(web/run app (options {:port 3000 :configuration builder}))
    (web/run app (options {:port 3000}))))
