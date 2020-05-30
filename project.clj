(defproject activity-bot "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://idle-bot.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [route-map "0.0.7-RC4"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-core "1.8.1"]
                 [ring "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [morse "0.4.3"]
                 [environ "1.1.0"]
                 [overtone/at-at "1.2.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "bot.jar"
  :profiles {:production {:env {:production true}}})
