(ns dbcp-tests
  (:require [jdbc.core :refer :all]
            [jdbc.pool.dbcp :as pool]
            [clojure.test :refer :all]))

(def dbspec {:subprotocol "h2"
             :subname ":mem:"})

(deftest datasource-spec
  (testing "Connection pool testing."
    (let [spec (pool/make-datasource-spec dbspec)]
      (is (instance? javax.sql.DataSource (:datasource spec)))
      (with-open [conn (make-connection spec)]
        (let [result (query conn ["SELECT 1 + 1 as foo;"])]
          (is (= [{:foo 2}] result)))))))
