(ns labradoodle.gitlab
  (:require [clojure.pprint :as pp]
            [clojure.java.data :refer [from-java]]
            [clojure.edn :as edn])
  (:import (org.gitlab.api GitlabAPI)))


; Helper Functions

(defn read-config []
  "Reads config file 'config.edn' from disk."
  (edn/read-string (slurp "config.edn")))

(defn- connect []
  (let [config (:gitlab (read-config))
        url (:url config)
        ; Private tokens are user-specific; they are listed under https://${url}/profile/account
        private-token (:private-token config)
        api (GitlabAPI/connect url private-token)]
    (.ignoreCertificateErrors api true)))

(defn inspect [obj]
  "Prints top-level props of obj as map"
  (doseq [key (keys (from-java obj))]
    (println key)))

; API Wrappers

(defn projects []
  (let [api (connect)]
    (.getProjects api)))

(defn project-names []
  (map (comp :name from-java) (projects)))

(defn project [id]
  "Return project with id; this is an API operation, so faster than finding by name."
  (let [api (connect)]
    (.getProject api (Integer. id))))

; todo: add "namespace" param ... proj names are not globally unique
(defn find-project [name]
  "Return project with name."
  (-> (filter #(= name (:name (from-java %))) (projects))
      first))

(defn issues [proj]
  (let [api (connect)]
    (.getIssues api proj)))

(defn print-proto2-issues []
  "An example of how labradoodle.gitlab functions can be composed to get back proto2 issues as EDN."
  (pp/pprint (from-java (issues (project 1)))))