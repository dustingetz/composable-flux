(ns flux.core
  (:require [clojure.browser.repl :as repl]))


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

  )




;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(println "Hello world!")
