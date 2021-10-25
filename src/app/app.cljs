(ns app.app
  (:require [keechma.next.controllers.router]
            [keechma.next.controllers.subscription]
            [app.controllers.counter]
            [app.controllers.proxy :as proxy]
            ["react-dom" :as rdom]))
(def app
  {:keechma.subscriptions/batcher rdom/unstable_batchedUpdates
   :keechma/controllers
   {:router
    #:keechma.controller {:params true
                          :type :keechma/router
                          :keechma/routes [["" {:page "home"}]
                                           ":page"
                                           ":page/:subpage"]}
    :counter
    #:keechma.controller {:params true}}})

(defn make-child-app [parent-app]
  {:keechma.subscriptions/batcher rdom/unstable_batchedUpdates
   :keechma/controllers
   {:counter (proxy/make parent-app :counter)
    :router (proxy/make parent-app :router)
    :derived-counter
    #:keechma.controller {:type :keechma/subscription
                          :params (fn [{:keys [counter]}]
                                    (* 2 counter))
                          :deps [:counter]}}})