package com.payu.kube.log.util

import javafx.beans.value.ObservableValue
import javafx.scene.Node

object ViewUtils {
    fun Node.bindManagedAndVisibility(observable: ObservableValue<Boolean>) {
        managedProperty().bind(visibleProperty())
        visibleProperty().bind(observable)
    }

    fun Node.toggleClass(className: String, isOn: Boolean) {
        if (isOn) {
            styleClass.addOnce(className)
        } else {
            styleClass.remove(className)
        }
    }

    private fun <E> MutableList<E>.addOnce(element: E) {
        if (!this.contains(element)) {
            this.add(element)
        }
    }
}