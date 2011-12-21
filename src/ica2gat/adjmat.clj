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

  (lower-triangle-matrix [amat]
    "Return a matrix containing the data of the lower triangle of the
     adjacency matrix, with zeros in the diagonal and upper triangle.
     The returned matrix is a sequence of sequences, each of the latter
     representing a row in the adjacency matrix. The first row contains
     the node names (row and column labels).
     Example: for a 3x3 matrix [[:A :B :C][0 0 0][4 0 0][7 8 0]]"
  )

  (matrix [amat]
    "Return the adjacency matrix as a sequence of sequences, each of
     the latter representing a row. The first row contains the node
     names (row and column labels). The diagonal is zeroed out.
     Example: for a 3x3 matrix [[:A :B :C][0 2 3][4 0 6][7 8 0]]"
  )

  (upper-triangle [amat]
    "Return a sequence of triplets, representing the upper triangle of
     the adjacency matrix, with the form: [node-name1 node-name2 weight]"
  )

  (upper-triangle-matrix [amat]
    "Return a matrix containing the data of the upper triangle of the
     adjacency matrix, with zeros in the diagonal and lower triangle.
     The returned matrix is a sequence of sequences, each of the latter
     representing a row in the adjacency matrix. The first row contains
     the node names (row and column labels).
     Example: for a 3x3 matrix [[:A :B :C][0 2 3][0 0 6][0 0 0]]"
  )

  (write-lower-triangle [amat]
    "Write the lower triangle of the adjacency matrix to the current
     value of *out* in labeled CSV form."
  )

  (write-lower-triangle-matrix [amat]
    "Write a matrix containing the lower triangle of the adjacency matrix
     to the current value of *out* in labeled CSV form. The diagonal and
     upper triangle are zeroed out."
  )

  (write-matrix [amat]
    "Write the adjacency matrix to the current value of *out* in labeled CSV form."
  )

  (write-upper-triangle [amat]
    "Write the upper triangle of the adjacency matrix to the current
     value of *out* in labeled CSV form."
  )

  (write-upper-triangle-matrix [amat]
    "Write a matrix containing the upper triangle of the adjacency matrix
     to the current value of *out* in labeled CSV form. The diagonal and
     lower triangle are zeroed out."
  )
)


(defrecord AdjacencyMatrix [nodes weights]
  AdjacencyMatrixProtocol

  (lower-triangle [amat]
    (let [ nodes (seq (.nodes amat))
           weights (.weights amat) ]
      (filter identity                      ; filter out nil elements
        (for [k1 nodes k2 nodes]            ; for all possible keys
          (if (> (.compareTo k1 k2) 0)      ; for lower triangle keys
            (if-let [w (get weights (seq [k1 k2]))]
              [k1 k2 w]))))))

  (lower-triangle-matrix [amat]
    (let [ nodes (seq (.nodes amat))
           amat-dim (count nodes)
           weights (.weights amat) ]
      (cons nodes                           ; prepend node name labels
        (partition amat-dim                 ; divide vector into rows
          (for [k1 nodes k2 nodes]          ; for all possible keys
            (if (> (.compareTo k1 k2) 0)    ; for lower triangle keys
              (get weights (seq [k1 k2]) 0) ; get actual weight
              0))))))                       ; return zero for all other keys

  (matrix [amat]
    (let [ nodes (seq (.nodes amat))
           amat-dim (count nodes)
           weights (.weights amat) ]
      (cons nodes                           ; prepend node name labels
        (partition amat-dim                 ; divide vector into rows
          (for [k1 nodes k2 nodes]          ; for all possible keys
            (get weights (seq [k1 k2]) 0))))))

  (upper-triangle [amat]
    (let [ nodes (seq (.nodes amat))
           weights (.weights amat) ]
      (filter identity                      ; filter out nil elements
        (for [k1 nodes k2 nodes]            ; for all possible keys
          (if (< (.compareTo k1 k2) 0)      ; for upper triangle keys
            (if-let [w (get weights (seq [k1 k2]))]
              [k1 k2 w]))))))

  (upper-triangle-matrix [amat]
    (let [ nodes (seq (.nodes amat))
           amat-dim (count nodes)
           weights (.weights amat) ]
      (cons nodes                           ; prepend node name labels
        (partition amat-dim                 ; divide vector into rows
          (for [k1 nodes k2 nodes]          ; for all possible keys
            (if (< (.compareTo k1 k2) 0)    ; for upper triangle keys
              (get weights (seq [k1 k2]) 0) ; get actual weight
              0))))))                       ; return zero for all other keys

  (write-lower-triangle [amat]
    (println "Source,Target,Type,Weight")
    (doseq [t (.lower-triangle amat)]
      (println (str (first t) "," (second t) ",Undirected," (nth t 2)))))

  (write-matrix [amat]
    (doseq [row (.matrix amat)]
      (let [cols (interpose ", " row)]
        (doseq [col cols] (print col))
        (println))))

  (write-lower-triangle-matrix [amat]
    (doseq [row (.lower-triangle-matrix amat)]
      (let [cols (interpose ", " row)]
        (doseq [col cols] (print col))
        (println))))

  (write-upper-triangle [amat]
    (println "Source,Target,Type,Weight")
    (doseq [t (.upper-triangle amat)]
      (println (str (first t) "," (second t) ",Undirected," (nth t 2)))))

  (write-upper-triangle-matrix [amat]
    (doseq [row (.upper-triangle-matrix amat)]
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
