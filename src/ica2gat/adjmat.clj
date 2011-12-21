(ns ica2gat.adjmat
  (:require [clojure.math.combinatorics :as comb])
  (:require [clojure.set :as set])
  (:gen-class)
)

(defprotocol AdjacencyMatrixProtocol
  "Defines a limited set of operations for Adjacency Matrices:
   returning vectors of node pairs from the upper and/or lower triangles
   of the adjacency matrix, writing the matrix, and returning the matrix."

  (lower-triangle [amat]
    "Return a sequence of node pairs from the lower triangle of the adjacency matrix"
  )

  (upper-triangle [amat]
    "Return a sequence of node pairs from the upper triangle of the adjacency matrix"
  )

  (matrix [amat]
    "Return a sequence of sequences of node pairs from adjacency matrix.
     Each sequence represents a row in the adjacency matrix, with the first
     row being the node names (row and column labels).
     Example: for a 3x3 matrix [[:A :B :C][1 2 3][4 5 6][7 8 9]]"
  )

  (write-matrix [amat]
    "Write the adjacency matrix to the current value of *out* in CSV form."
  )
)


(defrecord AdjacencyMatrix [node-names weights]
  AdjacencyMatrixProtocol

  (lower-triangle [amat] :lower-triangle)
  (upper-triangle [amat] :upper-triangle)

  (matrix [amat]
    (let [ node-names (seq (.node-names amat))
           amat-dim (count node-names)
           weights (.weights amat) ]
      (cons node-names
        (partition amat-dim
          (for [k1 node-names k2 node-names]
            (get weights (seq [k1 k2]) 0))))))

  (write-matrix [amat]
    (doseq [row (.matrix amat)]
      (let [cols (interpose ", " row)]
        (doseq [col cols] (print col))
        (println))))
)


(defn- make-pairs [nodes]
  "Return a sequence of all possible pairs of nodes, except pairs
   containing the same node twice."
  (filter (fn [[n1 n2]] (not= n1 n2))
          (comb/selections nodes 2)))

(defn make-adjacency-matrix [components]
  "Create and return an adjacency matrix from the given sequence
   of components (sets of node names)."
  (let [ nodes (sort (apply set/union components))
         pairs (mapcat make-pairs components)
         freq-map (frequencies pairs) ]
    (AdjacencyMatrix. nodes freq-map)))
