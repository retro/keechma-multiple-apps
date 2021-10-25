(ns app.ui.main
  (:require [keechma.next.helix.core :refer [with-keechma use-sub dispatch broadcast]]
            [keechma.next.helix.lib :refer [defnc]]
            [app.app :refer [make-child-app]]
            [keechma.next.core :as keechma]
            [keechma.next.helix.core :refer [KeechmaRoot]]
            [keechma.next.controllers.router :as router]
            [helix.hooks :as hooks]
            [helix.core :as hx :refer [$]]
            [helix.dom :as d]))

(defnc InnerApp [{:keechma/keys [app] :keys [children]}]
  {:wrap [with-keechma]}
  (let [inner-app-instance* (hooks/use-ref nil)
        [inner-app-generation set-inner-app-generation] (hooks/use-state 0)]
    (hooks/use-effect
     [app]
     (when-let [inner-app-instance @inner-app-instance*]
       (keechma/stop! inner-app-instance))
     (let [inner-app (make-child-app app)
           inner-app-instance (keechma/start! inner-app)]
       (reset! inner-app-instance* inner-app-instance)
       (set-inner-app-generation inc))
     (fn []
       (when-let [inner-app-instance @inner-app-instance*]
         (keechma/stop! inner-app-instance))))
    (when (pos? inner-app-generation)
      ($ KeechmaRoot {:keechma/app @inner-app-instance*} children))))

(defnc InnerMain [props]
  {:wrap [with-keechma]}
  (let [counter (use-sub props :counter)
        derived-counter (use-sub props :derived-counter)
        router (use-sub props :router)]
    (d/div
     "Current count " counter
     (d/br)
     "Current derived count " derived-counter
     (d/hr)
     (d/button
      {:onClick #(dispatch props :counter :inc)
       :class "border p-2 mr-2"}
      "INC INNER APP")
     (d/button
      {:onClick #(dispatch props :counter :dec)
       :class "border p-2 mr-2"}
      "DEC INNER APP")
     (d/button
      {:onClick #(broadcast props :dec)
       :class "border p-2 mr-2"}
      "BROADCAST DEC INNER APP")
     (d/hr)
     "CURRENT ROUTE " (pr-str router)
     (d/br)
     (d/a {:href (router/get-url props :router {:page "some-other-page"})} "Go to some other page"))))

(defnc Main [props]
  {:wrap [with-keechma]}
  (let [counter (use-sub props :counter)]
    (d/div
     "Current count " counter
     (d/hr)
     (d/button
      {:onClick #(dispatch props :counter :inc)
       :class "border p-2 mr-2"}
      "INC OUTER APP")
     (d/button
      {:onClick #(dispatch props :counter :dec)
       :class "border p-2 mr-2"}
      "DEC OUTER APP")
     (d/div
      {:class "m-2 border p-2"}
      ($ InnerApp
         ($ InnerMain)
         (d/div
          {:class "m-2 border p-2"}
          ($ InnerApp
             ($ InnerMain))))))))