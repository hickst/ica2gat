(ns ica2gat.core
  (:require clojure.contrib.combinatorics)
  (:require clojure.contrib.io)
  (:require clojure.set)
  (:gen-class)
)

(defn read-components [filename]
  (clojure.contrib.io/with-in-reader
    (clojure.java.io/file filename)
    (read)))

(defn make-pairs [aSet]
  (filter (fn [[n1 n2]] (not= n1 n2))
          (clojure.contrib.combinatorics/selections aSet 2)))

(defn write-pairs [pairs]
  (println "Source,Target,Type")
  (doseq [p pairs]
    (println (str (first p) "," (second p) ",Undirected"))))

(defn -main [ & args]
  (let [ filename (first args)
         components (read-components filename)
         nodes (sort (apply clojure.set/union components))
         pairs (mapcat make-pairs components) ]
    nodes
    ;; (write-pairs pairs)
  )
)

;; (-main "resources/sample-input-file")
