(ns ica2gat.core
  (:require [clojure.math.combinatorics :as comb])
  (:require [clojure.java.io :as io])
  (:require [clojure.set :as set])
  (:require [ica2gat.adjmat])
  (:gen-class)
)

(defn read-components [filename]
  (with-open [rdr (java.io.PushbackReader. (io/reader filename))]
    (read rdr)))

(defn make-pairs [aSet]
  (filter (fn [[n1 n2]] (not= n1 n2))
          (comb/selections aSet 2)))

(defn write-pairs [pairs]
  (println "Source,Target,Type")
  (doseq [p pairs]
    (println (str (first p) "," (second p) ",Undirected"))))

(defn -main [ & args]
  (let [ filename (first args)
         components (read-components filename)
         nodes (sort (apply set/union components))
         pairs (mapcat make-pairs components) ]
    (ica2gat.adjmat.AdjacencyMatrix. nodes {})
    ;; (write-pairs pairs)
  )
)

;; (-main "resources/sample-input-file")
