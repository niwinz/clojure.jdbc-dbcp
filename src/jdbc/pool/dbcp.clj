;; Copyright 2014 Andrey Antukh <niwi@niwi.be>
;;
;; Licensed under the Apache License, Version 2.0 (the "License")
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns jdbc.pool.dbcp
  (:require [jdbc.core :refer [uri->dbspec]])
  (:import org.apache.commons.dbcp2.BasicDataSource
           java.net.URI))

(defn- normalize-dbspec
  "Normalizes a dbspec for connection pool implementations."
  [{:keys [name vendor host port] :as dbspec}]
  (cond
   (or (string? dbspec) (instance? URI dbspec))
   (uri->dbspec dbspec)

   (and name vendor)
   (let [host   (or host "127.0.0.1")
         port   (if port (str ":" port) "")
         dbspec (dissoc dbspec :name :vendor :host :port)]
     (assoc dbspec
       :subprotocol vendor
       :subname (str "//" host port "/" name)))

   (map? dbspec)
   dbspec))

(defn make-datasource-spec
  "Given a plain dbspec, convert it on datasource dbspec
  using apachr commons connection pool implementation."
  [dbspec]
  (let [dbspec (normalize-dbspec dbspec)
        ds     (BasicDataSource.)]
    (if (:connection-uri dbspec)
      (.setUrl ds (:connection-uri dbspec))
      (.setUrl ds (str "jdbc:"
                       (:subprotocol dbspec) ":"
                       (:subname dbspec))))
    {:datasource (doto ds
                   (.setDriverClassName (:classname dbspec))
                   (.setUsername (:user dbspec))
                   (.setPassword (:password dbspec))
                   (.setInitialSize (:initial-size dbspec 0))
                   (.setMaxIdle (:max-idle dbspec 3))
                   (.setMaxTotal (:max-total dbspec 15))
                   (.setMinIdle (:min-idle dbspec 0))
                   (.setMaxWaitMillis (:max-wait-millis dbspec -1))
                   (.setTestOnCreate (:test-on-create dbspec false))
                   (.setTestOnBorrow (:test-on-borrow dbspec true))
                   (.setTestOnReturn (:test-on-return dbspec false))
                   (.setTestWhileIdle (:test-while-idle dbspec true))
                   (.setTimeBetweenEvictionRunsMillis (:time-between-eviction-runs-millis dbspec -1))
                   (.setNumTestsPerEvictionRun (:num-tests-per-eviction-run dbspec 3))
                   (.setMinEvictableIdleTimeMillis (:min-evictable-idle-time-millis dbspec (* 1000 60 30)))
                   (.setMaxConnLifetimeMillis (:max-conn-lifetime-millis dbspec (* 3600 1000)))
                   (.setValidationQuery (:validation-query dbspec nil)))}))
