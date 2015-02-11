(defproject labradoodle "0.1.0-SNAPSHOT"

  :description "Gitlab API Utilities"
  :url "http://github.com/roanosullivan/labradoodle"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]

                 [dk.ative/docjure "1.7.0"]

                 ; YIKES! 1.1.4 is the latest version deployed to Maven Central; need to manually build/install
                 ; YIKES! .. 1.1.8-SNAPSHOT from https://github.com/roanosullivan/java-gitlab-api (forked from
                 ; YIKES! .. https://github.com/timols/java-gitlab-api to fix issues linked from
                 ; YIKES! .. https://github.com/timols/java-gitlab-api/pull/46)
                 [org.gitlab/java-gitlab-api "1.1.8-SNAPSHOT"]

                 ; HUH? Is there a benefit to working with objects from "org.gitlab.api.models.*" package? Introduced
                 ; HUH? .. "clojure.java.data/from-java" because working with Map-like data structures is more fluid;
                 ; HUH? .. but ultimately may just want to replace "java-gitlab-api" and with clj-http.
                 [org.clojure/java.data "0.1.1"]]

  :main ^:skip-aot labradoodle.core

  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
