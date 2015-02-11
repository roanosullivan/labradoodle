(ns labradoodle.excel
  (:use [dk.ative.docjure.spreadsheet]))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Referenced while coding this ns:
;;  - http://clojure.org/cheatsheet
;;  - http://siscia.github.io/programming/2014/09/15/create-clojure-map-advanced-methods-part-1/
;;  - http://stackoverflow.com/questions/1676891/mapping-a-function-on-the-values-of-a-map-in-clojure
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(def columns (array-map :title "Task", :state "Status", [:milestone :title] "Milestone", [:assignee :name] "Assigned To"))

(defn fmap [f m]
  "Based on corresponding multi-method in https://github.com/clojure/algo.generic"
  (into {} (for [[k v] m] [k (f v)])))

(defn idx-comparator [v a b]
  "Sort order is based on order of elements in vector v"
  (let [idxa (.indexOf v a)
        idxb (.indexOf v b)]
    ;(println (str "A: " a "->" idxa))
    ;(println (str "B: " b "->" idxb))
    (compare idxa idxb)))

(defn cols->topkeys [cols]
  (let [ks (keys cols)]
    (map #(if (vector? %) (first %) %) ks)))

(defn ks->path [ks]
  "Ensures ks is a vector (since get-in expects vector as first arg)"
  (if (vector? ks)
    (rest ks)
    (vector ks)))

(defn resolve-nested-val [cols [k v :as entry]]
  "First, identifies key in cols (ie colsk) that corresponds to k. IF v is a map AND colsk is a
  vector, THEN returns modified entry with value v replaced by nested value (ie nested value
  that is referenced by colsk path), ELSE returns original entry."
  (let [topks (cols->topkeys cols)
        kidx (.indexOf topks k)
        colk (nth (keys cols) kidx)]
    (if (map? v)
      [k (get-in v (ks->path colk) v)]
      entry)))

(defn issues->rows [cols issues]
  "Produces rows array, with shape like this:
  [{:name \"Foo Widget\" :price 100}
   {:name \"Bar Widget\" :price 200}]"
  ; Create rows by transforming  issue objects as follows:
  ; (A) only contain keys from cols map;
  ; (B) resolves nested values for path keys (e.g. replaces milestone object with title if key is [:milestone :title])
  ; (C) values are all strings
  ; (D) entries are in same order as cols entries
  (let [topkeys (cols->topkeys cols)
        transform-issue (comp
                          #(into (sorted-map-by (partial idx-comparator topkeys)) %)
                          ;#(into (sorted-map) %)
                          #(fmap str %)
                          #(map (partial resolve-nested-val cols) %)
                          #(select-keys % topkeys))
        rows (map transform-issue issues)]
    (println "---------------")
    (clojure.pprint/pprint rows)
    rows))

(defn issues->celldata [issues]
  "Create celldata array, with shape like this:
  [[\"Name\" \"Price\"]
   [\"Foo Widget\" 100]
   [\"Bar Widget\" 200]]"
  (let [cols columns
        rows (issues->rows cols issues)
        data (into [(vals cols)] (map vals rows)) ]
    (println "---------------")
    (clojure.pprint/pprint data)
    data))

(defn issues->wb [proj issues]
  "Creates a spreadsheet containing data from issues"
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