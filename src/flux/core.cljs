(ns flux.core
  (:require [cats.core :as m]
            [cats.monad.identity :as id]
            [cats.labs.channel]
            [cljs.core.async :as a]
            [promesa.core :as p]
            [flux.util :refer [delayed-val]])
  (:require-macros [cljs.core.async.macros :as a]))


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

;; http://learnyouahaskell.com/a-fistful-of-monads
;; (<=<) :: (Monad m) => (b -> m c) -> (a -> m b) -> (a -> m c)
;; f <=< g = (\x -> (g x) >>= f)
(defn <=< [f g]
  #(m/bind (g %) f)) ;(comp g f)


;; bind will unwrap the f, return will wrap f with a journal
;; So the inner value is the updater-fn


(defn root-at [paths f]
  "Return a new updater-fn that applies f at a path in some bigger context.
If this is monadic, unwrap the updater-fn first, and journal that symbol and path."
  #(update-in % paths f))


(def load-records-pending (root-at [:pending] (constantly true)))

(def load-records-success (comp
                           (root-at [:records] (constantly [1 2 3]))
                           (root-at [:pending] (constantly false))))


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


;; writer monad will journal the symbol 'async-component-action,
;; and if actions are composed, we need to merge it into a tree-like
;; journal that shows us the composition
;; So our mjoin is a smart merge on the journal
;; want to use mappend





(comment
  (def update-fn (root-at [:count] inc))
  (def root-update-fn (root-at [:b] update-fn))
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
