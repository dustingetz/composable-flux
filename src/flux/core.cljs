(ns flux.core
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.labs.channel]
            [cljs.core.async :as a])
  (:require-macros [cljs.core.async.macros :as a]))


(def store (atom {:listViewA {:page 0 :records []}
                  :listViewB {:page 0 :records []}}))


(defn action-next-page [listview-state]
  (update-in listview-state [:page] inc))


(defn action-load-records [listview-state]
  (update-in listview-state [:records] (constantly [1 2 3])))


(def action-load-next-page (reduce comp [action-next-page action-load-records]))




(comment
  (action-next-page {:page 0 :records []})
  (action-load-records {:page 0 :records []})
  (action-load-next-page {:page 0 :records []})
  (swap! store update-in [:listViewA] action-load-next-page)

  (def v (m/bind (maybe/just 1) #(m/return (inc %))))
  @v

  )



(defn async-call
  "A function that emulates some asynchronous call."
  [n]
  (a/go
    (println "---> sending request" n)
    (a/<! (a/timeout n))
    (println "<--- receiving request" n)
    n))
