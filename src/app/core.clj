(ns app.core
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.core.async :refer [go]]
            [org.httpkit.server :as server]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [route-map.core :as rm]
            [clojure.string :as str]
            [app.bot :as bot])
  (:gen-class))

(defn parse-request [req]
  (json/parse-string (slurp req) true))

(def routes
  {"Token"  {:POST (fn [{body :body :as req}]
                     (let [token (:token (parse-request body))]
                       (do
                         (println "Token initialized")
                         (bot/set-token token))
                       {:status 200 :body "Token accepted"}))}
   "Start" {:GET (fn [_]
                   (if (bot/start-long-polling)
                     (do
                       (println "Bot is ready")
                       {:status 200 :body "Bot is ready"})
                     (do
                       (println "Token is not provided")
                       {:status 404 :body "Token is not provided"})))}
   "Stop" {:GET (fn [_]
                  (if (bot/stop-long-polling)
                    (do
                      (println "Bot has been stopped")
                      {:status 200 :body "Bot has been stopped"})
                    (do
                      (println "No active channels has been found")
                      {:status 404 :body "No active channels has been found"})))}})

(defn params-to-keyword [params]
  (reduce-kv (fn [acc k v]
               (assoc acc (keyword k) v))
             {} params))

(defn handler [{meth :request-method uri :uri :as req}]
  (if-let [res (rm/match [meth uri] routes)]
    ((:match res) (-> (assoc req :params (params-to-keyword (:params req)))
                      (update-in [:params] merge (:params res))))
    {:status 404 :body {:error "Not found"}}))

(defn preflight
  [{meth :request-method hs :headers :as req}]
  (let [headers (get hs "access-control-request-headers")
        origin (get hs "origin")
        meth  (get hs "access-control-request-method")]
    {:status 200
     :headers {"Access-Control-Allow-Headers" headers
               "Access-Control-Allow-Methods" meth
               "Access-Control-Allow-Origin" origin
               "Access-Control-Allow-Credentials" "true"
               "Access-Control-Expose-Headers" "Location, Transaction-Meta, Content-Location, Category, Content-Type, X-total-count"}}))

(defn allow [resp req]
  (let [origin (get-in req [:headers "origin"])]
    (update resp :headers merge
            {"Access-Control-Allow-Origin" origin
             "Access-Control-Allow-Credentials" "true"
             "Access-Control-Expose-Headers" "Location, Content-Location, Category, Content-Type, X-total-count"})))

(defn mk-handler [dispatch]
  (fn [{headers :headers uri :uri :as req}]
    (if (= :options (:request-method req))
      (preflight req)
      (let [resp (dispatch req)]
        (-> resp (allow req))))))

(def app
  (-> handler
      mk-handler
      wrap-params
      wrap-json-response
      wrap-reload))

(defonce state (atom nil))

(defn stop-server []
  (when-not (nil? @state)
    (@state :timeout 100)
    (reset! state nil)))

(defn start-server []
  (reset! state (server/run-server app {:port 9090})))

(defn restart-server [] (stop-server) (start-server))

(defn -main []
  (start-server)
  (println "Server started"))
