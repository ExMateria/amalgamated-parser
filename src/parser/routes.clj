(ns parser.routes
  (:require
    [io.pedestal.http.params :as p]
    [parser.datastore :as ds]
    [parser.parser :as pp]
    [clojure.string :as str]
    [parser.defaults :as d]
    [io.pedestal.http.body-params :as body-params]
    [cheshire.core :as json]
    [parser.schema :as sch]
    [clojure.stacktrace :as stk])
  (:import [java.io InputStreamReader InputStream]))

(defn respond-hello [req]
  {:status 200 :body "Jello, whirled."})

(def by-gender-output {:by-gender [[:Gender [::sch/Female]]]})

(def by-birthday-output {:by-birthday [[:DateOfBirth :asc]]})

(def by-name-output {:by-name [[:fullName :asc]]})

(defn gen-req
  [path alt req])



(defn create-records [ctx]
  (let [line (first (get (get ctx :json-params) :line ""))
        viable-delim (pp/first-delimiter-reaching-threshold
                       line
                       d/default-column-ordering
                       d/default-delimiters)]
    (try (doall
           (let [what (pp/parse-line line d/default-column-ordering viable-delim)
                 _ (do (println what))]
             {:status 201}))
         (catch Exception e (println (str "while processing " line " exception -\n" (ex-message e) "- " (stk/print-stack-trace e) "\nwas thrown"))
                            {:status 400}
                            ))
    ))




;["/user/:user-id/private" :post [inject-connection auth-required (body-params/body-params) view-user]]

;{1 [[:Gender [::sch/Female]] [:LastName :asc]]
;                         2 [[:DateOfBirth :asc]]
;                         3 [[:LastName :desc]]}
(defn get-records [req]
  (let [column (get req :path-params)
        records (ds/return-records (condp re-matches column
                                     #"(?i)gender" by-gender-output
                                     #"(?i)birthdate" by-birthday-output
                                     #"(?i)name" by-name-output
                                     :else {:error (str "Column " column " designated path parameter doesn't match any available sort parameters.")}
                                     ))]
    {:status 200 :body (json/generate-string {:records records})}
    ))

(def routes
  #{["/greet" :get respond-hello :route-name :greet]
    ["/records" :post [(body-params/body-params) create-records] :route-name :create-records]
    ["/records" :get get-records :route-name :get-records]
    ["/records/:column" :get get-records :route-name :records-by-column]})

