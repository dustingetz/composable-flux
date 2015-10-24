(ns flux.core
  (:require [cats.core :as m]
            [cats.builtin]
            [cats.monad.identity :as id]
            [cats.labs.channel]
            [cljs.core.async :as a]
            [promesa.core :as p]
            [flux.util :refer [delayed-val pipe-trans]]
            [cats.context :as ctx])
  (:require-macros [cljs.core.async.macros :as a]
                   [cats.context :as ctx]))


(def store (atom {:listViewA {:page 0 :records []}
                  :listViewB {:page 0 :records []}}))


;; An async action has to return a stream of updater fns. The dispatcher at the
;; top will pull updater-fns out of the stream one at a time and call
;; (swap! store updater-fn).
;;
;; Intermediate actions in the composition hierarchy can pull child updater-fns
;; out of child actions and compose them and return a new updater-fn that runs
;; the child updater-fn at the right path


;; action returns a loading state
;; then returns a state update and disables loading state


;; bind will unwrap the f, return will wrap f with a journal
;; So the inner value is the updater-fn


(defn root-at [paths mv]
  "Return a new updater-fn that applies f at a path.
If this is monadic, unwrap the updater-fn first, and journal that symbol and path."
  (ctx/with-context id/context
    (m/mlet [v mv]
      (m/return #(update-in % paths v)))))


(def load-records-pending (root-at [:pending] (id/identity (constantly true))))


;; IFn monoid is composition + identity
(def load-records-success (m/append
                           (root-at [:records] (id/identity (constantly [1 2 3])))
                           (root-at [:pending] (id/identity (constantly false)))))


(defn async-component-action [c]
  "put pending update, then async effect, then put success update"
  (a/go
    (a/>! c load-records-pending)
    (p/then (delayed-val 42)
            (fn [response]
              (a/go (a/>! c load-records-success))))))


;; writer monad will journal the symbol 'async-component-action,
;; and if actions are composed, we need to merge it into a tree-like
;; journal that shows us the composition
;; So our mjoin is a smart merge on the journal
;; want to use mappend


(defn consume-updates-forever [action-stream]
  (a/go
    (while true
      (let [mv (a/<! action-stream)]
        (m/mlet [v mv]
          (swap! store v))))))

(comment
  (do
    (def c (a/chan))
    (def cmp-action-stream c)
    (def root-action-stream
      (pipe-trans cmp-action-stream (map #(root-at [:listViewA] %))))
    (add-watch store :print (fn [k r old new] (print new)))
    (consume-updates-forever root-action-stream))

  (async-component-action c)

  )
