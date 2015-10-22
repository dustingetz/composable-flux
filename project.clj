(defproject flux "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [funcool/cats "1.0.0"]
                 [funcool/promesa "0.5.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 ;; for cider-jack-in-clojurescript
                 [com.cemerick/piggieback "0.2.1"]
                 [weasel "0.7.0"]
                 ]

  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-npm "0.6.1"]]
  :npm {:dependencies [[source-map-support "0.3.2"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release"]
  :target-path "target"
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  )
