(ns app.controllers.counter
  (:require [keechma.next.controller :as ctrl]))

(derive :counter :keechma/controller)

(defmethod ctrl/start :counter [_ _ _ _]
  0)

(defmethod ctrl/handle :counter [{:keys [state*]} ev _]
  (case ev
    :inc (swap! state* inc)
    :dec (swap! state* dec)
    nil))