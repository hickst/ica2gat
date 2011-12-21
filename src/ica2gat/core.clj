(ns ica2gat.core
  (:require [clojure.math.combinatorics :as comb])
  (:require [clojure.java.io :as io])
  (:require [clojure.set :as set])
  (:require [ica2gat.adjmat])
  (:gen-class)
)

(defn make-adjacency-matrix [nodes pairs]
  "Create and return an adjacency matrix from the given sequences of
   node names and node pairings."
  (let [freq-map (frequencies pairs)]
    (ica2gat.adjmat.AdjacencyMatrix. nodes freq-map)))

(defn make-pairs [nodes]
  "Return a sequence of all possible pairs of nodes, except pairs
   containing the same node twice."
  (filter (fn [[n1 n2]] (not= n1 n2))
          (comb/selections nodes 2)))

(defn read-components [filename]
  "Read the ICA components, as a Clojure data structure, directly from
   the specified file. The component data structure must be a sequence of
   sets. Each set contains the node names for the nodes of a single ICA
   component. Ex: [ #{LGN STG_R STG_L} #{Vermis LGN BA1} #{BA1 BA2 LGN} ]"
  (with-open [rdr (java.io.PushbackReader. (io/reader filename))]
    (read rdr)))

(defn write-pairs [pairs]
  (println "Source,Target,Type")
  (doseq [p pairs]
    (println (str (first p) "," (second p) ",Undirected"))))

(defn -main [ & args]
  (let [ filename (first args)
         components (read-components filename)
         nodes (sort (apply set/union components))
         pairs (mapcat make-pairs components) ]
    (make-adjacency-matrix nodes pairs)
    ;; (write-pairs pairs)
  )
)

;; (-main "resources/sample-input-file")
