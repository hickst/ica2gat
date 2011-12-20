(ns ica2gat.adjmat
  (:gen-class)
)

(defprotocol AdjacencyMatrixProtocol
  "Defines a limited set of operations for Adjacency Matrices: reading in
   pairs of nodes representing edges, returning vectors of node pairs from
   the upper and/or lower triangles of the adjacency matrix, and returning
   the matrix itself."

  (add-pairs [this pairs]
    "Add a sequence of node pairs, each representing an edge between adjacent nodes"
  )

  (lower-triangle [this]
    "Return a sequence of node pairs from the lower triangle of the adjacency matrix"
  )

  (upper-triangle [this]
    "Return a sequence of node pairs from the upper triangle of the adjacency matrix"
  )

  (matrix [this]
    "Return a sequence of sequences of node pairs from adjacency matrix.
     Each sequence represents a row in the adjacency matrix, with the first
     row being the node names (row and column labels).
     Example: for a 3x3 matrix [[:A :B :C][1 2 3][4 5 6][7 8 9]]"
  )
)


(defrecord AdjacencyMatrix [node-names weights])

(extend-type AdjacencyMatrix
  AdjacencyMatrixProtocol
  (add-pairs [_ pairs] :add-pairs)
  (lower-triangle [_] :lower-triangle)
  (upper-triangle [_] :upper-triangle)
  (matrix [_] :matrix)
)
