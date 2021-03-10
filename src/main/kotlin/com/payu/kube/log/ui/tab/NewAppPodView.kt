package com.payu.kube.log.ui.tab

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.ViewUtils.bindManagedAndVisibility
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class NewAppPodView: HBox() {

    val newestAppPodProperty = SimpleObjectProperty<PodInfo?>(null)

    var openPodAction: ((PodInfo) -> Unit)? = null

    private val appPodLabel: Label
    private val openPodButton: Button

    init {
        spacing = 10.0
        alignment = Pos.CENTER_LEFT
        bindManagedAndVisibility(newestAppPodProperty.isNotNull)

        appPodLabel = Label()
        appPodLabel.textProperty().bind(newestAppPodProperty.mapToString {
            "There is newer pod(${it?.name}) with this app(${it?.calculatedAppName})"
        })

        openPodButton = Button("Open")
        openPodButton.minWidth = USE_PREF_SIZE
        openPodButton.setOnAction {
            val newestPod = newestAppPodProperty.value ?: return@setOnAction
            openPodAction?.invoke(newestPod)
        }

        children.addAll(appPodLabel, openPodButton)
    }
}