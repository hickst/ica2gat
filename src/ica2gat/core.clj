(ns ica2gat.core
  (:require [clojure.java.io :as io])
  (:require [ica2gat.adjmat])
  (:gen-class)
)

(defn read-components [filename]
  "Read the ICA components, as a Clojure data structure, directly from
   the specified file. The component data structure must be a sequence of
   sets. Each set contains the node names for the nodes of a single ICA
   component. Ex: [ #{LGN STG_R STG_L} #{Vermis LGN BA1} #{BA1 BA2 LGN} ]"
  (with-open [rdr (java.io.PushbackReader. (io/reader filename))]
    (read rdr)))

(defn -main [ & args]
  (let [ filename (first args)
         components (read-components filename)
         amat (ica2gat.adjmat/make-adjacency-matrix components) ]
    ;; (.write-matrix amat)
    amat
  )
)

;; (-main "resources/sample-input-file")
