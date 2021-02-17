package com.payu.kube.log.util

import javafx.beans.value.ObservableValue
import javafx.scene.Node

object ViewUtils {
    fun Node.bindManagedAndVisibility(observable: ObservableValue<Boolean>) {
        managedProperty().bind(visibleProperty())
        visibleProperty().bind(observable)
    }
}