(ns flux.util
  (:require [promesa.core :as p]
            [cats.core :as m]
            [cljs.core.async :as a]))


(defn delayed-val [val]
  (p/promise (fn [deliver]
               (js/setTimeout #(deliver val) 1000))))


(comment
  (-> (m/fmap inc (delayed-val 42))
      (p/then (fn [v]
                (println v))))
  )



(defn pipe-trans
  [ci xf]
  (let [co (a/chan 1 xf)]
    (a/pipe ci co)
    co))



(comment
  ;; http://learnyouahaskell.com/a-fistful-of-monads
  ;; (<=<) :: (Monad m) => (b -> m c) -> (a -> m b) -> (a -> m c)
  ;; f <=< g = (\x -> (g x) >>= f)
  (defn <=< [f g]
    #_ (comp g f)

    (ctx/with-context id/context
      #_ (fn [x]
           (m/mlet [y (g x)]
             (m/return (f y))))
      (fn [x] (m/bind (g x) f)))
    ))
