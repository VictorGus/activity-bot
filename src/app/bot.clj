(ns app.bot
  (:require [morse.handlers :as m-h]
            [morse.api :as m-api]
            [morse.polling :as m-p]))

(def token (atom nil))

(defn set-token [t]
  (reset! token t))

(m-h/defhandler bot-api
  (m-h/command-fn "start" (fn [{{id :id :as chat} :chat}]
                            (println "Bot joined new chat: " chat)
                            (m-api/send-text @token id "Welcome!")))
  (m-h/command "help" {{id :id :as chat} :chat}
               (println "Help was requested in " chat)
               (m-api/send-text @token id "Help is on the way"))
  (m-h/message message (println "Intercepted message:" message)))

(def channel (atom nil))

(defn start-long-polling []
  (when @token
    (m-p/start @token bot-api)))

(defn stop-long-polling []
  (when @channel
    (m-p/stop @channel)))
