package com.payu.kube.log.ui.tab

import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.util.BindingsUtils.mapToBoolean
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.DateUtils.fullFormat
import com.payu.kube.log.util.ViewUtils.bindManagedAndVisibility
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font

class PodInfoView: HBox() {

    val podProperty = SimpleObjectProperty<PodInfo?>(null)

    private val statusIndicatorCircle: Circle
    private val namespaceLabel: Label
    private val statusLabel: Label
    private val timestampLabel: Label

    init {
        spacing = 10.0
        alignment = Pos.CENTER_LEFT
        bindManagedAndVisibility(podProperty.isNotNull)

        statusIndicatorCircle = Circle(5.0)
        statusIndicatorCircle.fillProperty().bind(
            Bindings.`when`(podProperty.mapToBoolean { it?.isReady ?: false })
                .then(Color.valueOf("#2bc140"))
                .otherwise(Color.valueOf("#f55e56"))
        )

        namespaceLabel = Label()
        namespaceLabel.isWrapText = true
        namespaceLabel.font = Font.font(12.0)
        namespaceLabel.textProperty()
            .bind(podProperty.mapToString { "${it?.namespace} - ${it?.containerImage}" })

        statusLabel = Label()
        statusLabel.minWidth = USE_PREF_SIZE
        statusLabel.font = Font.font(11.0)
        statusLabel.textProperty()
            .bind(podProperty.mapToString { "${it?.state?.long()} " +
                    "${it?.readyCount}/${it?.startedCount}/${it?.containerCount} " +
                    "R:${it?.restarts}" })

        val vbox = VBox()
        vbox.children.addAll(namespaceLabel, statusLabel)

        val emptyRegion = Region()
        setHgrow(emptyRegion, Priority.ALWAYS)

        timestampLabel = Label()
        timestampLabel.minWidth = USE_PREF_SIZE
        timestampLabel.font = Font.font(11.0)
        timestampLabel.textProperty()
            .bind(podProperty.mapToString { pod ->
                pod?.deletionTimestamp
                    ?.let { "C:${pod.creationTimestamp.fullFormat()}\nD:${it.fullFormat()}"}
                    ?: "C:${pod?.creationTimestamp?.fullFormat()}"
            })
        setHgrow(timestampLabel, Priority.NEVER)

        children.addAll(statusIndicatorCircle, vbox, emptyRegion, timestampLabel)
    }
}