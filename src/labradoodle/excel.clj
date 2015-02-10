(ns labradoodle.excel
  (:use [dk.ative.docjure.spreadsheet]))

; http://stackoverflow.com/questions/1676891/mapping-a-function-on-the-values-of-a-map-in-clojure
(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn map-key-comparator [m a b]
  "Sort order is based on order of keys in map m"
  (let [idxa (.indexOf (keys m) a)
        idxb (.indexOf (keys m) b)]
    ;(println (str "A: " a "->" idxa))
    ;(println (str "B: " b "->" idxb))
    (compare idxa idxb)))

(defn issues->rows [cols issues]
  "Produces rows array, with shape like this:
  [{:name \"Foo Widget\" :price 100}
   {:name \"Bar Widget\" :price 200}]"
  ; Create rows by transforming  issue objects as follows:
  ; (A) only contain keys from cols map;
  ; (B) values are all strings
  ; (C) entries are in same order as cols entries
  (let [transform-issue (comp
                          #(into (sorted-map-by (partial map-key-comparator cols)) %)
                          ;#(into (sorted-map) %)
                          #(fmap str %)
                          #(select-keys % (keys cols)))
        rows (map transform-issue issues)]
    (println "---------------")
    (clojure.pprint/pprint rows)
    rows))

(defn issues->celldata [issues]
  "Create celldata array, with shape like this:
  [[\"Name\" \"Price\"]
   [\"Foo Widget\" 100]
   [\"Bar Widget\" 200]]"
  (let [cols (array-map :title "Task", :state "Status", :milestone "Milestone")
        rows (issues->rows cols issues)
        data (into [(vals cols)] (map vals rows)) ]
    (println "---------------")
    (clojure.pprint/pprint data)
    data))

(defn issues->wb [proj issues]
  "Creates a spreadsheet containing data from issues"
  ;; http://clojure.org/cheatsheet
  (let [sheet-name (str (:name proj) " Issues")
        celldata (issues->celldata issues)
        wb (create-workbook sheet-name celldata)
        sheet (select-sheet sheet-name wb)
        header-row (first (row-seq sheet))]
    (set-row-style! header-row
                    (create-cell-style! wb {:background :yellow,
                                            :font {:bold true}}))
    wb))

(defn write-wb! [name wb]
  "Saves workbook to ${name}.xlsx"
  (save-workbook! (str name ".xlsx") wb))