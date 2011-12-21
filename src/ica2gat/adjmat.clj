(ns ica2gat.adjmat
  (:gen-class)
)

(defprotocol AdjacencyMatrixProtocol
  "Defines a limited set of operations for Adjacency Matrices: reading in
   pairs of nodes representing edges, returning vectors of node pairs from
   the upper and/or lower triangles of the adjacency matrix, and returning
   the matrix itself."

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
)
