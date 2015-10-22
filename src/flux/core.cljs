(ns flux.core
  (:require [cats.core :as m]
            [clojure.browser.repl :as repl]
            [cats.monad.maybe :as maybe]))


(def store (atom {:listViewA {:page 0 :records []}
                  :listViewB {:page 0 :records []}}))


(defn action-next-page [listview-state]
  (update-in listview-state [:page] inc))


(defn action-load-records [listview-state]
  (update-in listview-state [:records] (constantly [1 2 3])))


(def listview-reducer (reduce comp [action-next-page action-load-records]))




(comment
  (action-next-page {:page 0 :records []})
  (action-load-records {:page 0 :records []})
  (swap! store update-in [:listViewA] listview-reducer)

  (def v (m/bind (maybe/just 1) #(m/return (inc %))))
  @v

  )




;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(println "Hello world!")
