(ns ica2gat.core
  (:require [clojure.java.io :as io])
  (:require [clojure.tools.cli :as cli])
  (:require [ica2gat.adjmat])
  (:use [clojure.string :only (split)])
  (:gen-class)
)

(defn read-components [filename]
  "Read the ICA components into a Clojure data structure from
   the specified CSV file. Each line of the input data file represents
   the set of node names for the nodes of a single ICA.
   component. Example input file with 3 components:
      LGN, STG_R, STG_L
      BA1, BA2
      STG_L, LGN, BA1, BA45"
  (let [ lines (line-seq (io/reader filename))
         comma-ws #"(\,|\s)+" ]             ; regexp to match commas and whitespace
    (map #(set (split % comma-ws)) lines)))


(defn make-output-type-writer [basename output-type]
  (let [ofilename (str basename "_" (name output-type) ".csv")]
    (io/writer ofilename)))


(defn -main [ & args]
  (let [ usage "Usage: java -jar ica2gat.jar (-glt | -gut | -mat | -mlt | -mut)... -o outfile-basename infile-name"
         [options other-args flag-usage]
           (cli/cli args
             ["-glt" "Output a list of lower triangle data tuples (for Gephi)" :flag true]
             ["-gut" "Output a list of upper triangle data tuples (for Gephi)" :flag true]
             ["-h"   "Show usage message for this program" :flag true]
             ["-mat" "Output an adjacency matrix (for BCT in MATLAB)" :flag true]
             ["-mlt" "Output lower triangle of adjacency matrix (for BCT in MATLAB)" :flag true]
             ["-mut" "Output upper triangle of adjacency matrix (for BCT in MATLAB)" :flag true]
             ["-o" "The basename (without extension) for the output file(s)"]) ]

    ;; if user asks for help, print usage messages and exit
    (if (:h options)
      (do
        (println usage)
        (println flag-usage)
        (System/exit 1)))

    ;; check for any missing arguments
    (if (not-any? identity (map #(% options) #{:glt :gut :mat :mlt :mut}))
      (do
        (println "ERROR: Required output type argument is missing.")
        (println usage)
        (println flag-usage)
        (System/exit 2)))

    (if (not (:o options))
      (do
        (println "ERROR: Required output file basename argument is missing.")
        (println usage)
        (println flag-usage)
        (System/exit 3)))

    (let [infile (first other-args)]
      (if (nil? infile)
        (do
          (println "ERROR: Required input filename argument is missing.")
          (println usage)
          (println flag-usage)
          (System/exit 4)))

      ;; create an adjacency matrix using the given inputs
      (let [ components (read-components infile)
             amat (ica2gat.adjmat/make-adjacency-matrix components)
             flag-method-map {:glt ica2gat.adjmat/write-lower-triangle
                              :gut ica2gat.adjmat/write-upper-triangle
                              :mat ica2gat.adjmat/write-matrix
                              :mlt ica2gat.adjmat/write-lower-triangle-matrix
                              :mut ica2gat.adjmat/write-upper-triangle-matrix} ]

        ;; write an output file for each given output type flag
        (doseq [key (keys flag-method-map)]
          (if (key options)
            (let [ wrtr (make-output-type-writer (:o options) key)
                   method (key flag-method-map) ]
              (binding [*out* wrtr] (doto amat method)))))

        amat)))
  )

;; (ns ica2gat.core)
;; (load "core")
;; (def am (-main "-mat" "-o" "basename" "resources/simple"))
