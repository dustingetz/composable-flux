(ns flux.util
  (:require [promesa.core :as p]
            [cats.core :as m]))


(defn delayed-val [val]
  (p/promise (fn [deliver]
               (js/setTimeout #(deliver val) 10000))))


(comment
  (-> (m/fmap inc (delayed-val 42))
      (p/then (fn [v]
                (println v))))
  )
