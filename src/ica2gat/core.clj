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


(defn write-output [amat basename output-type method]
  "Write an output file for the given output-type using the given method"
  (let [ ofilename (str basename "_" (name output-type) ".csv")
         wrtr (io/writer ofilename) ]
    (binding [*out* wrtr] (doto amat method))))


(defmulti write-output-type (fn [output-type _ _] output-type))

(defmethod write-output-type :glt [output-type amat basename]
  (write-output amat basename :glt ica2gat.adjmat/write-lower-triangle))

(defmethod write-output-type :gut [output-type amat basename]
  (write-output amat basename :gut ica2gat.adjmat/write-upper-triangle))

(defmethod write-output-type :mat [output-type amat basename]
  (write-output amat basename :mat ica2gat.adjmat/write-matrix))

(defmethod write-output-type :mlt [output-type amat basename]
  (write-output amat basename :mlt ica2gat.adjmat/write-lower-triangle-matrix))

(defmethod write-output-type :mut [output-type amat basename]
  (write-output amat basename :mut ica2gat.adjmat/write-upper-triangle-matrix))


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

      ;; create an adjacency matrix using the given inputs and
      ;; write an output file for each given output type flag
      (let [ components (read-components infile)
             amat (ica2gat.adjmat/make-adjacency-matrix components) ]
        (doseq [output-type #{:glt :gut :mat :mlt :mut}]
          (if (output-type options)
            (write-output-type output-type amat (:o options))))
        amat)))
  )

;; (ns ica2gat.core)
;; (load "core")
;; (def am (-main "-mat" "-o" "basename" "resources/simple"))
