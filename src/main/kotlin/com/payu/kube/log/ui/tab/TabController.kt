package com.payu.kube.log.ui.tab

import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.*
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.GlobalKeyEventHandlerService
import com.payu.kube.log.service.coloring.StyledText
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.logs.PodLogsWatcher
import com.payu.kube.log.service.pods.PodChangeInterface
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.pods.PodWithAppInterface
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.ui.MainController
import com.payu.kube.log.ui.tab.list.LogEntryCell
import com.payu.kube.log.ui.tab.list.LogListView
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.LoggerUtils.logger
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import java.net.URL
import java.util.*
import java.util.concurrent.ForkJoinPool
import kotlin.concurrent.fixedRateTimer
import kotlin.streams.toList


class TabController(
    monitoredPod: PodInfo,
    private val podStoreService: PodStoreService,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val stylingTextService: StylingTextService,
    private val mainController: MainController,
    private val searchQueryCompilerService: SearchQueryCompilerService
) : Initializable, EventHandler<KeyEvent>, PodChangeInterface, PodWithAppInterface {

    companion object {
        private val CLOSE_TAB_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.W, KeyCodeCombination.SHORTCUT_DOWN)
        private val COPY_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.C, KeyCodeCombination.SHORTCUT_DOWN)
        private val FIND_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.F, KeyCodeCombination.SHORTCUT_DOWN)
        private val CLEAR_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.C)
        private val WRAP_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.W)
        private val AUTOSCROLL_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.A)
    }

    private val log = logger()

    @FXML
    lateinit var tab: Tab

    @FXML
    lateinit var podInfoView: PodInfoView

    @FXML
    lateinit var autoscrollCheckbox: CheckBox

    @FXML
    lateinit var wrapCheckbox: CheckBox

    @FXML
    lateinit var clearButton: Button

    @FXML
    lateinit var searchBox: SearchBoxView

    @FXML
    lateinit var logListView: LogListView

    @FXML
    lateinit var newAppPodView: NewAppPodView

    private var timer: Timer? = null
    private var podLogsWatcher: PodLogsWatcher? = null

    private val monitoredPodProperty = SimpleObjectProperty(monitoredPod)
    private val newestAppPodProperty = SimpleObjectProperty<PodInfo?>(null)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        log.info("initialize - ${monitoredPodProperty.value.name}")
        tab.setOnClosed {
            globalKeyEventHandlerService.unregisterKeyPressEventHandler(this)
            stopMonitor()
        }
        globalKeyEventHandlerService.registerKeyPressEventHandler(this)

        autoscrollCheckbox.selectedProperty().addListener { _, _, newValue ->
            if (newValue) {
                logListView.scrollToEnd()
            }
        }

        wrapCheckbox.selectedProperty().addListener { _, _, _ ->
            logListView.refresh()
            if (autoscrollCheckbox.isSelected) {
                logListView.scrollToEnd()
            }
        }

        clearButton.setOnAction { logListView.clear() }

        setupSearch()
        setupLogList()
        setupMonitoredPod()
        setupNewestAppPod()
        startMonitor()
    }

    private fun setupSearch() {
        searchBox.isVisible = false
        searchBox.searchAction = this::search
        searchBox.queryFactory = searchQueryCompilerService::compile
    }

    private fun setupLogList() {
        logListView.stylingTextService = stylingTextService
        logListView.onAddToSearch = this::addToSearch
        logListView.wrapTextProperty.bind(wrapCheckbox.selectedProperty())
        logListView.searchProperty.bind(searchBox.searchProperty)
    }

    private fun setupMonitoredPod() {
        tab.textProperty().bind(monitoredPodProperty.mapToString { it.name })
        podInfoView.podProperty.bind(monitoredPodProperty)
    }

    private fun setupNewestAppPod() {
        newAppPodView.newestAppPodProperty.bind(newestAppPodProperty)
        newAppPodView.openPodAction = {
            mainController.openPod(it)
        }
    }

    override fun handle(event: KeyEvent) {
        if (!tab.isSelected) {
            return
        }
        when {
            CLOSE_TAB_KEY_CODE_COMBINATION.match(event) -> {
                tab.onClosed?.handle(Event(Tab.CLOSED_EVENT))
                tab.tabPane.tabs.remove(tab)
                event.consume()
            }
            COPY_KEY_CODE_COMBINATION.match(event) -> {
                logListView.copySelectionToClipboard()
                event.consume()
            }
            CLEAR_KEY_CODE_COMBINATION.match(event) -> {
                logListView.clear()
                event.consume()
            }
            WRAP_KEY_CODE_COMBINATION.match(event) -> {
                wrapCheckbox.isSelected = !wrapCheckbox.isSelected
                event.consume()
            }
            AUTOSCROLL_KEY_CODE_COMBINATION.match(event) -> {
                autoscrollCheckbox.isSelected = !autoscrollCheckbox.isSelected
                event.consume()
            }
            FIND_KEY_CODE_COMBINATION.match(event) -> {
                searchBox.isVisible = !searchBox.isVisible
                searchBox.requestFocusSearchField()
                event.consume()
            }
        }
    }

    private fun startMonitor() {
        val monitoredPod = monitoredPodProperty.value
        newestAppPodProperty.set(
            podStoreService.getNewestPodForApp(monitoredPod.calculatedAppName)
                ?.takeIf { it != monitoredPod }
        )
        podStoreService.startWatchPod(monitoredPod, this)
        podStoreService.startWatchApp(monitoredPod.calculatedAppName, this)

        podLogsWatcher = PodLogsWatcher(monitoredPod)
        podLogsWatcher?.start()
        timer = fixedRateTimer(daemon = true, period = 300) {
            val newLines = podLogsWatcher?.getNewLogs()
                ?.takeIf { it.isNotEmpty() } ?: return@fixedRateTimer
            updateList(newLines)
        }
    }

    private fun updateList(newElements: List<String>) {
        newElements.asSequence()
            .chunked(100)
            .forEach { chunk ->
                val newStyledLines = ForkJoinPool(3).submit<List<StyledText>> {
                    chunk.parallelStream()
                        .map { stylingTextService.styleText(it, LogEntryCell.RULES) }
                        .toList()
                }.get()
                addNewLinesToList(newStyledLines)
            }
    }

    private fun addNewLinesToList(styledTexts: List<StyledText>) {
        Platform.runLater {
            logListView.addLines(styledTexts)
            if (autoscrollCheckbox.isSelected) {
                logListView.scrollToEnd()
            }
        }
    }

    override fun onPodChange(pod: PodInfo) {
        monitoredPodProperty.set(pod)
        podLogsWatcher?.onPodChange(pod)
    }

    override fun onNewPodWithApp(pod: PodInfo) {
        newestAppPodProperty.set(pod.takeIf { pod != monitoredPodProperty.value })
    }

    private fun search(search: SearchBoxView.Search?) {
        logListView.refresh()
        search ?: return
        Platform.runLater {
            Thread.sleep(50)
            autoscrollCheckbox.isSelected = false
            val indexToScroll = logListView.indexOfLast { search.query.check(it.text) } ?: return@runLater
            Platform.runLater {
                logListView.selectionModel.select(indexToScroll)
                logListView.scrollUntilVisible(indexToScroll)
            }
        }
    }

    fun addToSearch(type: SearchBoxView.AddToSearchType, text: String) {
        searchBox.addToSearch(type, text)
        searchBox.isVisible = true
        searchBox.requestFocusSearchField()
    }

    private fun stopMonitor() {
        podStoreService.stopWatchPod(monitoredPodProperty.value, this)
        podStoreService.stopWatchApp(monitoredPodProperty.value.calculatedAppName, this)
        podLogsWatcher?.stop()
        timer?.cancel()
    }
}

