(ns parser.schema
  (:require [clojure.spec.alpha :as s]
            [clj-time.core :as ts]
            [clj-time.format :as fmt]))

(s/def ::Male (s/and
                string?
                #(nil? (re-find #"(?i)f" %))))

(s/def ::Female (s/and
                  string?
                  #(re-find #"(?i)f" %)))


(s/def ::Gender (s/or :male #(s/valid? ::Male %)
                      :female #(s/valid? ::Female %)))

(s/def ::FirstName string?)
(s/def ::LastName string?)

;; TODO maybe move to a more appropriate place?
(defn try-parse-date [date-string] (let [list-of-date-times (flatten
                                                         (map
                                                           (fn [formatter]
                                                             (try
                                                               [(fmt/parse formatter date-string)]
                                                               (catch Exception e [])))
                                                           (vals fmt/formatters)))]
                                     (if (empty? list-of-date-times)
                                       nil
                                       (first list-of-date-times))))

(s/def ::DateOfBirth (s/and string?
                            (fn [date-string] (try-parse-date date-string)))
  ; TODO - potentially implement some checks
  ; on either formatting or on insuring the date is not after today
  ;(fn [dateString] (let [now (java.time.LocalDateTime/now)
  ;                                          this-year (.getYear now)
  ;                                          find-year-matches ]))
  )

(s/def ::Row (s/keys :req-un [::Gender ::DateOfBirth ::FirstName ::LastName ::FavoriteColor]))


(s/def ::InputFileString (fn [fileString] (.exists (clojure.java.io/file (str fileString)))))
(s/def ::InputFileStringArgs (s/or :single ::InputFileString
                                   :many (s/coll-of ::InputFileString)))

(s/def ::ProperCLIInput (fn [cliinput]
                          (and (s/valid? ::InputFileString (first cliinput))
                               (reduce
                                 (fn [up-to-now incoming]
                                   (and (s/valid? ::CLIOption incoming) up-to-now))
                                 true
                                 (rest cliinput)))))