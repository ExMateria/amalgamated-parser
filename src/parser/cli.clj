(ns parser.cli
  (:require [clojure.tools.cli :as c]
            [clojure.spec.alpha :as s]
            [parser.schema :as sch]
            [parser.parser :as p]
            [parser.defaults :as d]
            [parser.datastore :as ds]
            [clojure.string :as str]))

(defn verify-all [opts args]
  (let [args-not-valid? [(s/valid? ::sch/InputFileStringArgs args)]
        ;_ (do (println (str "args not valid is " args-not-valid?)))
        ;{:keys [addparsers parsers outprefix]} opts
        ] (reduce (fn [up-to-now n] (and up-to-now n)) false args-not-valid?)))

(defn -main [& args]
  (let [{:keys [options
                arguments
                summary
                errors]} (c/parse-opts args
                                       [["-h" "--[no-]help HELP US ALL!!!" ""
                                         :default (str false)]
                                        ["-a" "--addparsers WHAT" ""
                                         :assoc-fn vector]
                                        ["-d" "--delimiters WHO" ""
                                         :default d/default-delimiters
                                         :assoc-fn vector]
                                        ["-p" "--prefixoutputfile HOW" ""
                                         :default "output_"
                                         :assoc-fn vector]
                                        ["-o" "--outputSorts WHY" ""
                                         :default d/default-output-map
                                         :assoc-fn vector]])
        slurped (str/split-lines (slurp (java.io.FileReader. (first arguments))))
        {:keys [delimiters outputSorts prefixoutputfile]} (first options)
        {:keys [addparsers]} (into {} (map #(into [] %) (partition-all 2 (rest options))))
        attempt-parse-store-and-results (let [all-delimiters (into delimiters (if (coll? addparsers) addparsers [addparsers]))
                                              viable-delimiter (p/first-delimiter-reaching-threshold (first slurped) d/default-column-ordering all-delimiters)
                                              parsed-if-possible (cond
                                                                   (= 1 (count all-delimiters)) (doall (map (fn [line] (p/parse-line line d/default-column-ordering (first all-delimiters))) slurped))
                                                                   viable-delimiter (doall (map (fn [line] (p/parse-line line d/default-column-ordering viable-delimiter)) slurped))
                                                                   :else nil)]
                                          (if parsed-if-possible
                                            (ds/return-records d/default-output-map)))]
    (when (or (:help options)
              (verify-all options args))
      (println summary))
    (println (str "slurped - " slurped "\nwith args - " arguments "\nand opts - " options "\ngives results of - \n" (str/join "\n\n" (map (fn [res] (str/join ",\n" res)) attempt-parse-store-and-results))))))
