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
import com.payu.kube.log.service.pods.PodWithAppInterface
import com.payu.kube.log.util.BindingsUtils.mapToBoolean
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.DateUtils.fullFormat
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.util.ViewUtils.bindManagedAndVisibility
import com.payu.kube.log.view.CustomListViewSkin
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.text.Text
import java.net.URL
import java.util.*


class TabController(
    monitoredPod: PodInfo,
    private val podStoreService: PodStoreService,
    private val podLogStoreService: PodLogStoreService,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val stylingTextService: StylingTextService,
    private val mainController: MainController
) : Initializable, EventHandler<KeyEvent>, PodChangeInterface, PodWithAppInterface {
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

    @FXML
    lateinit var appPodBox: HBox

    @FXML
    lateinit var appPodLabel: Label

    @FXML
    lateinit var openNewestAppPodButton: Button

    lateinit var searchTextProperty: StringBinding

    private val monitoredPodProperty = SimpleObjectProperty(monitoredPod)
    private val newestAppPodProperty = SimpleObjectProperty<PodInfo?>(null)
    private val logsList = FXCollections.observableArrayList<String>()
    private var clearIndex = 0

    private val listChangeListener = ListChangeListener<String> {
        updateList(it.list)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        log.info("initialize - ${monitoredPodProperty.value.name}")
        setupSearch()

        tab.setOnClosed {
            globalKeyEventHandlerService.unregisterKeyPressEventHandler(this)
            stopMonitor()
        }
        globalKeyEventHandlerService.registerKeyPressEventHandler(this)

        clearButton.setOnAction { clear() }

        setupLogList()

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
        setupMonitoredPod()
        setupNewestAppPod()
        startMonitor()
    }

    private fun setupSearch() {
        searchBox.managedProperty().bind(searchBox.visibleProperty())
        searchBox.isVisible = false
        searchTextProperty = Bindings.`when`(searchBox.visibleProperty())
            .then(searchTextField.textProperty())
            .otherwise("")
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
    }

    private fun setupLogList() {
        logListView.skin = CustomListViewSkin(logListView)
        logListView.items = logsList
        logListView.cellFactory = Callback {
            LogEntryCell(stylingTextService, wrapCheckbox.selectedProperty(), searchTextProperty)
        }
        logListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        setupLogListContextMenu()
    }

    private fun setupLogListContextMenu() {
        val contextMenu = ContextMenu()

        val copyItem = MenuItem("Copy")
        copyItem.setOnAction {
            val value = (it.source as? MenuItem)?.userData as? String ?: return@setOnAction
            setClipboardContent(value)
        }
        contextMenu.items.add(copyItem)

        val copyLineItem = MenuItem("Copy line")
        copyLineItem.setOnAction {
            copySelectionToClipboard(logListView)
        }
        contextMenu.items.add(copyLineItem)

        val clearBeforeItem = MenuItem("Clear before")
        clearBeforeItem.setOnAction {
            val minSelectionIndex = logListView.selectionModel.selectedIndices.firstOrNull() ?: return@setOnAction
            clearIndex += minSelectionIndex
            logsList.remove(0, minSelectionIndex)
        }
        contextMenu.items.add(clearBeforeItem)

        logListView.contextMenu = contextMenu
        logListView.setOnContextMenuRequested { contextMenuEvent ->
            val line = logListView.selectionModel.selectedItems.firstOrNull() ?: return@setOnContextMenuRequested
            val index = (contextMenuEvent.target as? Text)?.userData as? Int ?: return@setOnContextMenuRequested
            val text = stylingTextService.calcSegmentForIndex(line, LogEntryCell.RULES, index)
            copyItem.isVisible = text != null
            copyItem.userData = text
        }
    }

    private fun setupMonitoredPod() {
        tab.textProperty().bind(monitoredPodProperty.mapToString { it.name })
        statusIndicatorCircle.fillProperty().bind(
            Bindings.`when`(monitoredPodProperty.mapToBoolean { it.isReady })
                .then(Color.valueOf("#2bc140"))
                .otherwise(Color.valueOf("#f55e56"))
        )
        namespaceLabel.textProperty()
            .bind(monitoredPodProperty.mapToString { "${it.namespace} - ${it.containerImage}" })
        statusLabel.textProperty()
            .bind(monitoredPodProperty.mapToString { "${it.state.long()} " +
                    "${it.readyCount}/${it.startedCount}/${it.containerCount} " +
                    "R:${it.restarts}" })
        timestampLabel.textProperty()
            .bind(monitoredPodProperty.mapToString { pod ->
                pod.deletionTimestamp
                    ?.let { "C:${pod.creationTimestamp.fullFormat()}\nD:${it.fullFormat()}"}
                    ?: "C:${pod.creationTimestamp.fullFormat()}"
            })
    }

    private fun setupNewestAppPod() {
        appPodBox.bindManagedAndVisibility(newestAppPodProperty.isNotNull)
        appPodLabel.textProperty().bind(newestAppPodProperty.mapToString {
            "There is newer pod(${it?.name}) with this app(${it?.calculatedAppName})"
        })
        openNewestAppPodButton.setOnAction {
            val newestPod = newestAppPodProperty.value ?: return@setOnAction
            mainController.openPod(newestPod)
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
        newestAppPodProperty.set(
            podStoreService.getNewestPodForApp(monitoredPodProperty.value.calculatedAppName)
                ?.takeIf { it != monitoredPodProperty.value }
        )
        clearIndex = 0
        podStoreService.startWatchPod(monitoredPodProperty.value, this)
        podStoreService.startWatchApp(monitoredPodProperty.value.calculatedAppName, this)
        podLogStoreService.startLogging(monitoredPodProperty.value)
        val logsList = podLogStoreService.podLogs[monitoredPodProperty.value]
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
        monitoredPodProperty.set(pod)
    }

    override fun onNewPodWithApp(pod: PodInfo) {
        newestAppPodProperty.set(pod.takeIf { pod != monitoredPodProperty.value })
    }

    private fun search() {
        logListView.refresh()

        val text = searchTextProperty.get()

        if (text.isNotBlank()) {
            val index = logsList.indexOfLast {
                text in it
            }
            if (index >= 0) {
                autoscrollCheckbox.isSelected = false
                (logListView.skin as? CustomListViewSkin<*>)
                    ?.getCustomFlow()
                    ?.forceScrollTo(index)
            }
        }
    }

    private fun clear() {
        clearIndex += logsList.size
        logsList.clear()
    }

    private fun stopMonitor() {
        podStoreService.stopWatchPod(monitoredPodProperty.value, this)
        podStoreService.stopWatchApp(monitoredPodProperty.value.calculatedAppName, this)
        podLogStoreService.podLogs[monitoredPodProperty.value]?.removeListener(listChangeListener)
        podLogStoreService.stopLogging(monitoredPodProperty.value)
        logsList.clear()
    }

    private fun copySelectionToClipboard(listView: ListView<*>) {
        val clipboardString = StringBuilder()
        val selectedItems = listView.selectionModel.selectedItems ?: listOf()
        for (item in selectedItems) {
            clipboardString.append(item?.toString())
            clipboardString.append("\n")
        }
        setClipboardContent(clipboardString.toString())
    }

    private fun setClipboardContent(text: String) {
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(text)
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }
}

