(ns parser.defaults
  (:require [clojure.spec.alpha :as s]
            [parser.schema :as sch]
            [clojure.string :as str]
            [clj-time.format :as fmt]))

(def default-output-map {1 [[:Gender [::sch/Female]] [:LastName :asc]]
                         2 [[:DateOfBirth :asc]]
                         3 [[:LastName :desc]]})

(def default-column-ordering [:LastName
                              :FirstName
                              :Gender
                              :FavoriteColor
                              :DateOfBirth])

(def default-time-formatter (fmt/formatter "MM/dd/yyyy"))

(def default-formatting {
                         :fullName      identity
                         :LastName      identity
                         :FirstName     identity
                         :Gender        (fn [gender-string] (->> gender-string
                                                                 (s/conform ::sch/Gender)
                                                                 (first)
                                                                 (str)
                                                                 ((fn [kwd-string] (str/split kwd-string #":")))
                                                                 (second)))
                         :FavoriteColor identity
                         :DateOfBirth   (fn [date-string] (let [parse-attempt (sch/try-parse-date date-string)]
                                                            (if parse-attempt
                                                              (fmt/unparse default-time-formatter parse-attempt)
                                                              (str "Date parsing failed for - " date-string))))})

(defn apply-format [format-r format-e]
  (into {} (map (fn [[ek ev]] [ek ((ek format-r) ev)]) format-e)))

(defn default-formatter [format-e]
  (let [formatter (partial apply-format default-formatting)]
    (update-in format-e [:parsed] formatter)))

(def default-delimiters [" \\| " ", " " "])
