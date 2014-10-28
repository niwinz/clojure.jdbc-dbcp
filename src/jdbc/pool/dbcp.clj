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

(defrecord DataSource [datasource]
  java.io.Closeable
  (close [_]
    (.close datasource)))

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
    (->DataSource (doto ds
                    (.setDriverClassName (:classname dbspec))
                    (.setUsername (:user dbspec))
                    (.setPassword (:password dbspec))

                    ;; Pool Size Management
                    (.setInitialSize (:initial-pool-size dbspec 0))
                    (.setMaxIdle (:max-pool-idle dbspec 3))
                    (.setMaxTotal (:max-pool-size dbspec 15))
                    (.setMinIdle (:min-pool-size dbspec 0))
                    (.setMaxWaitMillis (:max-wait dbspec -1))

                    ;; Connection eviction
                    (.setMaxConnLifetimeMillis (:max-connection-lifetime dbspec 3600000))

                    ;; Connection testing
                    (.setValidationQuery (:test-connection-query dbspec nil))
                    (.setTestOnBorrow (:test-connection-on-borrow dbspec false))
                    (.setTestOnReturn (:test-connection-on-return dbspec false))
                    (.setTestWhileIdle (:test-connection-while-idle dbspec true))
                    (.setTimeBetweenEvictionRunsMillis (:test-idle-connections-period dbspec 800000))
                    (.setNumTestsPerEvictionRun 4)
                    (.setMinEvictableIdleTimeMillis (:max-connection-idle-lifetime dbspec 1800000))))))
