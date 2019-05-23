(ns parser.parser-test
  (:require [clojure.test :refer :all])
  (:require [parser.parser :refer [parse-line]]
            [clojure.string :as str]
            [parser.defaults :refer [default-column-ordering]]))

(def test-parse-line-row-1 [["one" "fifteen" "two" "dkwen" "x23lkjlsk"]])
(def random-delim " *& ")

(deftest parse-line-bare-bones-test
  (is
    (parse-line (str/join random-delim test-parse-line-row-1) default-column-ordering random-delim)
    (zipmap default-column-ordering test-parse-line-row-1)))
