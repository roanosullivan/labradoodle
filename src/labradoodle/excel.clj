(ns labradoodle.excel
  (:use [dk.ative.docjure.spreadsheet])
  (:require [clojure.string :as s :refer [trim join split]])
  (:import (clojure.lang ISeq)))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Referenced while coding this ns:
;;  - http://clojure.org/cheatsheet
;;  - http://siscia.github.io/programming/2014/09/15/create-clojure-map-advanced-methods-part-1/
;;  - http://stackoverflow.com/questions/1676891/mapping-a-function-on-the-values-of-a-map-in-clojure
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Cell Data Definition
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(def columns (array-map :group ""
                        :id "ID"
                        :title "Outstanding Item",
                        :description "Notes",
                        :labels "Tags"
                        [:milestone :title] "Milestone",
                        :state "Status",
                        [:assignee :name] "Assigned To"))

; groups are based on Issue labels
(def groups [{:display "Development" :label "dev"},
             {:display "Ops" :label "ops"},
             {:display "Demo Prep" :label "demo"},
             {:display "Documentation" :label "docs"}])

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Data Transformation functions
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

(defn render [v]
  "Renders value based on type"
  (cond
    (instance? ISeq v) (join ", " v)
    :else (str v)))

(defn issues->rows [cols issues]
  "Produces rows array, with shape like this:
  [{:name \"Foo Widget\" :price 100}
   {:name \"Bar Widget\" :price 200}]"
  ; Create rows by transforming  issue objects as follows:
  ; (A) add placeholder key for group column
  ; (B) only contain keys from cols map;
  ; (C) resolves nested values for path keys (e.g. replaces milestone object with title if key is [:milestone :title])
  ; (D) render values (by default converts to strings)
  ; (E) entries are in same order as cols entries
  (let [topkeys (cols->topkeys cols)
        transform-issue-fn (comp
                             #(into (sorted-map-by (partial idx-comparator topkeys)) %)
                             ;#(into (sorted-map) %)
                             #(fmap render %)
                             #(map (partial resolve-nested-val cols) %)
                             #(select-keys % topkeys)
                             #(merge % {:group ""}))
        rows (map transform-issue-fn issues)]
    (println "---------------")
    (clojure.pprint/pprint rows)
    rows))

(defn any-label? [labels row]
  "return true if one of row :labels contains any items from labels set, otherwise return false"
  (let [row-labels (map trim (split (:labels row) #","))]
    (some labels row-labels)))

(defn has-label? [label row]
  "return true if one of row :labels is label, otherwise return false"
  (any-label? #{label} row))

(defn group-rows [groups rows]
  "Transforms rows vector into new vector with rows re-arranged (and possibly duplicated)
  per groups definition. Each group of rows is demarcated by a group-header-row containing
  a single group-header object."
  (let [reduce-fn (fn [grouped-rows {:keys [label display :as group]}]
                    (let [group-header-row (vector {:group display})
                          group-rows (filter #(has-label? label %) rows)]
                      (-> grouped-rows
                          (into group-header-row)
                          (into group-rows))))
        group-labels (set (map :label groups))
        misc (filter #(not (any-label? group-labels %)) rows)]
    (-> (reduce reduce-fn [] groups)
        (into (vector {:group "Misc"}))
        (into misc))))

(defn group-fixed-rows [rows]
  "Groups rows by labels: dev, ops, demo, docs. Include any ungrouped rows under a misc group."
  {:deprecated "This fn hardcodes the groups; use the more flexible group-rows fn instead."}
  (let [dev (filter #(has-label? "dev" %) rows)
        ops (filter #(has-label? "ops" %) rows)
        demo (filter #(has-label? "demo" %) rows)
        docs (filter #(has-label? "docs" %) rows)
        misc (filter #(not (any-label? #{"dev", "ops", "demo", "docs"} %)) rows)]
    (-> []
        (into (vector {:group "Development"}))
        (into dev)
        (into (vector {:group "Ops"}))
        (into ops)
        (into (vector {:group "Demo Prep"}))
        (into demo)
        (into (vector {:group "Documentation"}))
        (into docs)
        (into (vector {:group "Misc"}))
        (into misc))))

(defn issues->celldata [issues]
  "Create celldata array, with shape like this:
  [[\"Name\" \"Price\"]
   [\"Foo Widget\" 100]
   [\"Bar Widget\" 200]]"
  (let [cols columns
        rows (issues->rows cols issues)
        groups (group-rows groups rows)
        data (into [(vals cols)] (map vals groups)) ]
    (println "---------------")
    (clojure.pprint/pprint data)
    data))


;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Workbook Manipulation fuctions
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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