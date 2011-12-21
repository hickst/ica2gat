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
    "Return a sequence of triplets, representing the lower triangle of
     the adjacency matrix, with the form: [node-name1 node-name2 weight]"
  )

  (matrix [amat]
    "Return a sequence of sequences of node pairs from adjacency matrix.
     Each sequence represents a row in the adjacency matrix, with the first
     row being the node names (row and column labels).
     Example: for a 3x3 matrix [[:A :B :C][1 2 3][4 5 6][7 8 9]]"
  )

  (upper-triangle [amat]
    "Return a sequence of triplets, representing the upper triangle of
     the adjacency matrix, with the form: [node-name1 node-name2 weight]"
  )

  (write-lower-triangle [amat]
    "Write the lower triangle of the adjacency matrix to the current
     value of *out* in labeled CSV form."
  )

  (write-matrix [amat]
    "Write the adjacency matrix to the current value of *out* in labeled CSV form."
  )

  (write-upper-triangle [amat]
    "Write the upper triangle of the adjacency matrix to the current
     value of *out* in labeled CSV form."
  )
)


(defrecord AdjacencyMatrix [nodes weights]
  AdjacencyMatrixProtocol

  (lower-triangle [amat]
    (let [ nodes (seq (.nodes amat))
           weights (.weights amat) ]
      (filter identity
        (for [k1 nodes k2 nodes]
          (if (> (.compareTo k1 k2) 0)
            (if-let [w (get weights (seq [k1 k2]))]
              [k1 k2 w]))))))

  (matrix [amat]
    (let [ nodes (seq (.nodes amat))
           amat-dim (count nodes)
           weights (.weights amat) ]
      (cons nodes
        (partition amat-dim
          (for [k1 nodes k2 nodes]
            (get weights (seq [k1 k2]) 0))))))

  (upper-triangle [amat]
    (let [ nodes (seq (.nodes amat))
           weights (.weights amat) ]
      (filter identity
        (for [k1 nodes k2 nodes]
          (if (< (.compareTo k1 k2) 0)
            (if-let [w (get weights (seq [k1 k2]))]
              [k1 k2 w]))))))

  (write-lower-triangle [amat]
    (println "Source,Target,Type,Weight")
    (doseq [t (.lower-triangle amat)]
      (println (str (first t) "," (second t) ",Undirected," (nth t 2)))))

  (write-matrix [amat]
    (doseq [row (.matrix amat)]
      (let [cols (interpose ", " row)]
        (doseq [col cols] (print col))
        (println))))

  (write-upper-triangle [amat]
    (println "Source,Target,Type,Weight")
    (doseq [t (.upper-triangle amat)]
      (println (str (first t) "," (second t) ",Undirected," (nth t 2)))))
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
