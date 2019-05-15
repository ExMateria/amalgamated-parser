(ns parser.parser
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [parser.schema :as sch]
            [parser.datastore :as ds]))

(defn first-delimiter-reaching-threshold [line column-ordering delim-candidates]
  (let [needed-delimiter-count-base (- (count column-ordering) 1)
        needed-delimiter-count (if (< needed-delimiter-count-base 1) 0 needed-delimiter-count-base)
        all-delims-matching-needed-count (filter
                                           (fn [pattern]
                                             (<= needed-delimiter-count
                                                 (count (re-seq (re-pattern pattern) line))))
                                           delim-candidates)
        first-matching-delim (if (empty? all-delims-matching-needed-count)
                               nil
                               (first all-delims-matching-needed-count))]
    first-matching-delim))

(defn parse-line [line column-ordering delim]
  (let [parsed (str/split line (re-pattern delim))
        mapped-values (zipmap column-ordering parsed)
        values-match-spec (s/valid? ::sch/Row mapped-values)]
    (if values-match-spec
      (ds/save-records {:valid  true
                        :parsed mapped-values})
      (let [spec-failure-explained (s/explain ::sch/Row mapped-values)
            _ (do
                (println
                  (str "The following row -> " line
                       " failed to meet spec:\n"
                       spec-failure-explained)))]
        (ds/save-records {:valid                  false
                          :parsed                 mapped-values
                          :spec-failure-explained spec-failure-explained})))))