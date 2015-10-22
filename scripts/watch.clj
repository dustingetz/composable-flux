(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'flux.core
   :output-to "out/flux.js"
   :output-dir "out"})
