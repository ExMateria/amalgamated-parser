(ns parser.routes)

(defn respond-hello [req]
  {:status 200 :body "Jello, whirled."})

(defn create-records [req]

  )

(defn get-records [req]

  )

(def routes
  #{["/greet" :get respond-hello :route-name :greet]
    ["/records" :post create-records :route-name :records]
    ["/records" :get get-records :route-name :get-records]
    ["/records/:column" :get get-records :route-name :records]})

