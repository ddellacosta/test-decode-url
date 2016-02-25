(defproject test-decode-url "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[clj-http "2.1.0"]
                 [compojure "1.4.0"]
                 [org.clojure/clojure "1.7.0"]
                 [org.immutant/immutant "2.1.2"]
                 [ring/ring-defaults "0.1.5"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler test-decode-url.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
