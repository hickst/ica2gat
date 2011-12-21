(ns ica2gat.core
  (:require [clojure.java.io :as io])
  (:require [clojure.tools.cli :as cli])
  (:require [ica2gat.adjmat])
  (:gen-class)
)

(defn read-components [filename]
  "Read the ICA components, as a Clojure data structure, directly from
   the specified file. The component data structure must be a sequence of
   sets. Each set contains the node names for the nodes of a single ICA
   component. Ex: [ #{LGN STG_R STG_L} #{Vermis LGN BA1} #{BA1 BA2 LGN} ]"
  (with-open [rdr (java.io.PushbackReader. (io/reader filename))]
    (read rdr)))


(defn -main [ & args]
  (let [ usage "Usage: java -jar ica2gat.jar [-d] (-glt | -gut | -mat | -mlt | -mut)... -o outfile-basename infile-name"
         [options other-args flag-usage]
           (cli/cli args
             ["-d"   "Indicates the input contains directed nodes" :flag true]
             ["-glt" "Output a list of lower triangle data tuples (for Gephi)" :flag true]
             ["-gut" "Output a list of upper triangle data tuples (for Gephi)" :flag true]
             ["-h"   "Show usage message for this program" :flag true]
             ["-mat" "Output an adjacency matrix (for BCT in MATLAB)" :flag true]
             ["-mlt" "Output lower triangle of adjacency matrix (for BCT in MATLAB)" :flag true]
             ["-mut" "Output upper triangle of adjacency matrix (for BCT in MATLAB)" :flag true]
             ["-o" "The basename (without extension) for the output file(s)"]) ]
    (if (:h options)                        ; if user asks for help
      (do                                   ; then print usage messages
        (println usage)
        (println flag-usage))
      (if-let [infile (first other-args)]   ; else get input file
        (let [ components (read-components infile)
              amat (ica2gat.adjmat/make-adjacency-matrix components) ]
          amat)
        (do
          (println "ERROR: Required input filename missing.")
          (println usage)
          (println flag-usage)))))
)

;; (-main "resources/sample-input-file")
