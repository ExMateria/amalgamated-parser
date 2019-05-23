(ns parser.datastore
  (:require [parser.schema :as sch]
            [clojure.spec.alpha :as s]
            [parser.defaults :as d]))

; TODO - move this into a component to deal with state


(def datastore
  (atom (hash-map)))

(defn supplement-record [rec]
  (let [{:keys [parsed]} rec
        {:keys [FirstName LastName Gender DateOfBirth]} parsed
        hashed-key (hash (str FirstName LastName Gender (sch/try-parse-date DateOfBirth)))
        full-name (str FirstName " " LastName)]
    {hashed-key (-> rec
                    (assoc-in [:parsed :fullName] full-name)
                    (assoc-in [:committime] (java.time.LocalDateTime/now)))}))

(defn save-records [rec]
  (let [supplemented (supplement-record rec)]
    (swap! datastore merge supplemented)))

(defn rudimentary-sort-from-vector-or-plain-old-sort
  "Sort via grouping the results from the collection by the values in the vector"
  [possible-values-ordering-or-x-scend-indicator
   key-to-order-on
   coll]
  (if (coll? possible-values-ordering-or-x-scend-indicator)
    (flatten (reduce (fn [[prior-sort-result prior-sort-left-overs] next-sort-priority]
                       (let [haves (into
                                     []
                                     (filter (fn [m]
                                               (s/valid?
                                                 next-sort-priority
                                                 (key-to-order-on m)))
                                             prior-sort-left-overs))
                             havenots (into
                                        []
                                        (filter (fn [m]
                                                  (not
                                                    (s/valid?
                                                      next-sort-priority
                                                      (key-to-order-on m))))
                                                prior-sort-left-overs))]
                         [(into prior-sort-result haves)
                          (into prior-sort-left-overs havenots)]
                         ))
                     [[] coll]
                     possible-values-ordering-or-x-scend-indicator))
    (if (= :desc possible-values-ordering-or-x-scend-indicator)
      (reverse (sort-by key-to-order-on coll))
      (sort-by key-to-order-on coll))))

(defn return-records [sorting]
  (let [ds @datastore
        datastorevals (vals ds)]
    (if (empty? ds)
      {}
      (map (fn [[id sor]]
             (map d/default-formatter
                  (reduce (fn [acc [key-to-sort-on vec-or-x-scend]]
                            (rudimentary-sort-from-vector-or-plain-old-sort
                              vec-or-x-scend
                              key-to-sort-on
                              datastorevals))
                          {} sor)))
           sorting))))


;(merge accumulated-records {id (rudimentary-sort-from-vector-or-plain-old-sort
;                                 ordering-or-x-scend key-to-sort-on (vals @datastore))})