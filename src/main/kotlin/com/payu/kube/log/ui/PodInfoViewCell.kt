package com.payu.kube.log.ui

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.util.DateUtils.fullFormat

class PodInfoViewCell : ListCell<PodInfo?>() {

    @FXML
    lateinit var statusIndicatorCircle: Circle

    @FXML
    lateinit var podNameLabel: Label

    @FXML
    lateinit var stateLabel: Label

    @FXML
    lateinit var creationTimestampLabel: Label

    private var root: Parent? = null
    private var fxmlLoader: FXMLLoader? = null

    override fun updateItem(item: PodInfo?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            text = null
            graphic = null
        } else {
            if (fxmlLoader == null) {
                val loader = FXMLLoader(javaClass.getResource("/fxmls/podInfoViewCell.fxml"))
                loader.setController(this)
                root = loader.load<Parent>()
                fxmlLoader = loader
            }

            statusIndicatorCircle.fill = if (item.isReady) Color.valueOf("#2bc140") else Color.valueOf("#f55e56")

            podNameLabel.text = item.name
            stateLabel.text = "${item.state.short()} " +
                    "${item.readyCount}/${item.startedCount}/${item.containerCount} " +
                    "R:${item.restarts}"
            creationTimestampLabel.text = item.creationTimestamp.fullFormat()

            text = null
            graphic = root
        }
    }
}