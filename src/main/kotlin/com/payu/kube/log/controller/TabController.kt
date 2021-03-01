package com.payu.kube.log.controller

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Callback
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.GlobalKeyEventHandlerService
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.logs.PodLogStoreService
import com.payu.kube.log.service.pods.PodChangeInterface
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.util.DateUtils.fullFormat
import com.payu.kube.log.util.LoggerUtils.logger
import java.net.URL
import java.util.*


class TabController(
    private var monitoredPod: PodInfo,
    private val podStoreService: PodStoreService,
    private val podLogStoreService: PodLogStoreService,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val stylingTextService: StylingTextService
) : Initializable, EventHandler<KeyEvent>, PodChangeInterface {
    private val log = logger()
    private val closeTabKeyCodeCompanion = KeyCodeCombination(KeyCode.W, KeyCodeCombination.SHORTCUT_DOWN)
    private val copyKeyCodeCompanion = KeyCodeCombination(KeyCode.C, KeyCodeCombination.SHORTCUT_DOWN)
    private val findCompanion = KeyCodeCombination(KeyCode.F, KeyCodeCombination.SHORTCUT_DOWN)
    private val clearKeyCodeCompanion = KeyCodeCombination(KeyCode.C)
    private val wrapKeyCodeCompanion = KeyCodeCombination(KeyCode.W)
    private val autoscrollKeyCodeCompanion = KeyCodeCombination(KeyCode.A)

    private val executeSearchKeyCodeCompanion = KeyCodeCombination(KeyCode.ENTER)
    private val clearSearchCodeCombination = KeyCodeCombination(KeyCode.ESCAPE)

    @FXML
    lateinit var tab: Tab

    @FXML
    lateinit var namespaceLabel: Label

    @FXML
    lateinit var statusLabel: Label

    @FXML
    lateinit var timestampLabel: Label

    @FXML
    lateinit var statusIndicatorCircle: Circle

    @FXML
    lateinit var autoscrollCheckbox: CheckBox

    @FXML
    lateinit var wrapCheckbox: CheckBox

    @FXML
    lateinit var clearButton: Button

    @FXML
    lateinit var searchBox: HBox

    @FXML
    lateinit var searchTextField: TextField

    @FXML
    lateinit var logListView: ListView<String>

    lateinit var searchTextProperty: StringBinding
    private var logsList = FXCollections.observableArrayList<String>()
    private var clearIndex = 0

    private val listChangeListener = ListChangeListener<String> {
        updateList(it.list)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        log.info("initialize - ${monitoredPod.name}")
        searchBox.managedProperty().bind(searchBox.visibleProperty())
        searchBox.isVisible = false
        searchTextProperty = Bindings.`when`(searchBox.visibleProperty())
            .then(searchTextField.textProperty())
            .otherwise("")

        tab.setOnClosed {
            globalKeyEventHandlerService.unregisterKeyPressEventHandler(this)
            stopMonitor()
        }
        globalKeyEventHandlerService.registerKeyPressEventHandler(this)

        clearButton.setOnAction { clear() }

        logListView.items = logsList
        logListView.cellFactory = Callback {
            LogEntryCell(stylingTextService, wrapCheckbox.selectedProperty(), searchTextProperty)
        }
        logListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        logListView.contextMenu = ContextMenu().apply {
            val copyItem = MenuItem("Copy")
            copyItem.setOnAction {
                copySelectionToClipboard(logListView)
            }
            this.items.add(copyItem)
            val clearBeforeItem = MenuItem("Clear before")
            clearBeforeItem.setOnAction {
                val minSelectionIndex = logListView.selectionModel.selectedIndices.minOrNull() ?: return@setOnAction
                clearIndex += minSelectionIndex
                logsList.remove(0, minSelectionIndex)
            }
            this.items.add(clearBeforeItem)
        }

        autoscrollCheckbox.selectedProperty().addListener { _, _, newValue ->
            if (newValue && logsList.size > 0) {
                logListView.scrollTo(logsList.lastIndex)
            }
        }

        wrapCheckbox.selectedProperty().addListener { _, _, _ ->
            logListView.refresh()
            if (autoscrollCheckbox.isSelected && logsList.size > 0) {
                logListView.scrollTo(logsList.lastIndex)
            }
        }

        searchTextProperty.addListener { _ ->
            search()
        }

        searchTextField.setOnKeyPressed {
            if (clearSearchCodeCombination.match(it)) {
                searchTextField.text = ""
                it.consume()
            } else if (executeSearchKeyCodeCompanion.match(it)) {
                search()
                it.consume()
            }
        }

        startMonitor()
    }

    private fun search() {
        logListView.refresh()

        val text = searchTextProperty.get()

        if (text.isNotBlank()) {
            var index = logsList.indexOfLast {
                text in it
            }
            if (index >= 0) {
                index -= 2
                if (index < 0) {
                    index = 0
                }
                autoscrollCheckbox.isSelected = false
                logListView.scrollTo(index)
            }
        }
    }

    override fun handle(event: KeyEvent) {
        if (!tab.isSelected) {
            return
        }
        when {
            closeTabKeyCodeCompanion.match(event) -> {
                tab.onClosed?.handle(Event(Tab.CLOSED_EVENT))
                tab.tabPane.tabs.remove(tab)
                event.consume()
            }
            copyKeyCodeCompanion.match(event) -> {
                copySelectionToClipboard(logListView)
                event.consume()
            }
            clearKeyCodeCompanion.match(event) -> {
                clear()
                event.consume()
            }
            wrapKeyCodeCompanion.match(event) -> {
                wrapCheckbox.isSelected = !wrapCheckbox.isSelected
                event.consume()
            }
            autoscrollKeyCodeCompanion.match(event) -> {
                autoscrollCheckbox.isSelected = !autoscrollCheckbox.isSelected
                event.consume()
            }
            findCompanion.match(event) -> {
                searchBox.isVisible = !searchBox.isVisible
                if (searchBox.isVisible) {
                    searchTextField.requestFocus()
                }
                event.consume()
            }
        }
    }

    private fun startMonitor() {
        this.onPodChange(monitoredPod)
        clearIndex = 0
        podStoreService.startWatchPod(monitoredPod, this)
        podLogStoreService.startLogging(monitoredPod)
        val logsList = podLogStoreService.podLogs[monitoredPod]
        updateList(logsList)
        logsList?.addListener(listChangeListener)
    }

    private fun updateList(list: List<String>?) {
        val listToShow = (list ?: listOf()).drop(clearIndex)
        val newElements = listToShow.drop(logsList.size)
        logsList.addAll(newElements)
        if (autoscrollCheckbox.isSelected && logsList.size > 0) {
            logListView.scrollTo(logsList.lastIndex)
        }
    }

    override fun onPodChange(pod: PodInfo) {
        monitoredPod = pod
        tab.text = monitoredPod.name
        statusIndicatorCircle.fill = if (pod.isReady) Color.valueOf("#2bc140") else Color.valueOf("#f55e56")
        namespaceLabel.text = "${pod.namespace} - ${pod.containerImage}"
        statusLabel.text = "${pod.state.long()} " +
                "${pod.readyCount}/${pod.startedCount}/${pod.containerCount} " +
                "R:${pod.restarts}"
        timestampLabel.text = pod.deletionTimestamp
            ?.let { "C:${pod.creationTimestamp.fullFormat()}\nD:${it.fullFormat()}"}
            ?: "C:${pod.creationTimestamp.fullFormat()}"
    }

    private fun clear() {
        clearIndex += logsList.size
        logsList.clear()
    }

    private fun stopMonitor() {
        podStoreService.stopWatchPod(monitoredPod, this)
        podLogStoreService.podLogs[monitoredPod]?.removeListener(listChangeListener)
        podLogStoreService.stopLogging(monitoredPod)
        logsList.clear()
    }

    private fun copySelectionToClipboard(listView: ListView<*>) {
        val clipboardString = StringBuilder()
        val selectedItems = listView.selectionModel.selectedItems ?: listOf()
        for (item in selectedItems) {
            clipboardString.append(item?.toString())
            clipboardString.append("\n")
        }
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(clipboardString.toString())
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }
}

