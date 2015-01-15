(defproject clojure.jdbc/clojure.jdbc-dbcp "0.3.2"
  :description "Apache Commons JDBC connection pool implementation for clojure.jdbc."
  :url "http://github.com/niwibe/clojure.jdbc-dbcp"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.apache.commons/commons-dbcp2 "2.0.1"]]

  :profiles {:dev {:dependencies [[clojure.jdbc "0.4.0-beta1"]
                                  [com.h2database/h2 "1.3.176"]]}})

