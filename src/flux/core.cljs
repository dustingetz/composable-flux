(ns flux.core
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.labs.channel]
            [cljs.core.async :as a]
            [promesa.core :as p])
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




;; An async action has to return a stream of updater fns. The dispatcher at the
;; top will pull updater-fns out of the stream one at a time and call
;; (swap! store updater-fn).
;;
;; Intermediate actions in the composition hierarchy can pull child updater-fns
;; out of child actions and compose them and return a new updater-fn that runs
;; the child updater-fn at the right path


;; action returns a loading state
;; then returns a state update and disables loading state


(defn delayed-val [val]
  (p/promise (fn [deliver]
               (js/setTimeout #(deliver val) 10000))))


(comment
  (-> (m/fmap inc (delayed-val 42))
      (p/then (fn [v]
                (println v))))
  )


(def load-records-pending #(update-in % [:pending] (constantly true)))

(def load-records-success (comp
                           #(update-in % [:records] (constantly [1 2 3]))
                           #(update-in % [:pending] (constantly false))))


(defn async-component-action []
  "put pending update, then async effect, then put success update"
  (let [c (a/chan)]
    (a/go
      (a/>! c load-records-pending)
      (p/then (delayed-val 42)
              (fn [response]
                (a/go (a/>! c load-records-success))))
      )
    c))


(defn refine [paths f]
  #(update-in % paths f))


(comment
  (def update-fn #(update-in % [:count] inc))
  (def root-update-fn (refine [:b] update-fn))
  (root-update-fn {:a {:count 0}
                   :b {:count 0}})
  )


(defn pipe-trans
  [ci xf]
  (let [co (a/chan 1 xf)]
    (a/pipe ci co)
    co))

(defn consume-updates-forever [action-stream]
  (a/go
    (while true
      (let [update-fn (a/<! action-stream)]
        (swap! store update-fn)))))

(comment
  (def cmp-action-stream (async-component-action))

  (def root-action-stream
    (pipe-trans cmp-action-stream (map (partial refine [:listViewA]))))

  (add-watch store :print (fn [k r old new] (print new)))

  (consume-updates-forever root-action-stream)

  )
