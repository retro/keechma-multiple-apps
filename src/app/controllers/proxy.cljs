(ns app.controllers.proxy
  (:require [keechma.next.controller :as ctrl]
            [keechma.next.core :as core]))

(derive ::controller :keechma/controller)

(defmethod ctrl/api ::controller [{::keys [app controller-name]}]
  (-> app
      (core/get-api* controller-name)
      deref))

(defmethod ctrl/init ::controller [{::keys [app controller-name] :keys [state*] :as ctrl}]
  (let [unsubscribe (core/subscribe app controller-name (fn [val] (ctrl/transact ctrl #(reset! state* val))))]
    (assoc ctrl ::unsubscribe unsubscribe)))

(defmethod ctrl/start ::controller [{::keys [app controller-name]} _ _ _]
  (core/get-derived-state app controller-name))

(defmethod ctrl/handle ::controller [{::keys [app controller-name]} ev payload]
  (when-not (= "keechma.on" (namespace ev))
    (binding [core/*transaction-depth* 0]
      (core/dispatch app controller-name ev payload))))

(defmethod ctrl/terminate ::controller [{::keys [unsubscribe]}]
  (unsubscribe))

(defn make [app controller-name]
  {:keechma.controller/params true
   :keechma.controller/type ::controller
   ::app app
   ::controller-name controller-name})