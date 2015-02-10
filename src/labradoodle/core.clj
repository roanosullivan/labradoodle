(ns labradoodle.core
  (:require [labradoodle.gitlab :as gl]
            [labradoodle.excel :as ex]
            [clojure.java.data :refer [from-java]])
  (:gen-class))

(defn export-punchlist [proj-name]
  "Exports open issues from specified Gitlab project into a punchlist-formatted Excel workbook.
  Param proj should specify the project _name_."
  (let [proj-obj (gl/find-project proj-name)
        proj (from-java proj-obj)
        issues (from-java (gl/issues proj-obj))
        wb (ex/issues->wb proj issues)]
    (ex/write-wb! "punchlist" wb)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
