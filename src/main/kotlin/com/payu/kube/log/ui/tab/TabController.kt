package com.payu.kube.log.ui.tab

import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.*
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.GlobalKeyEventHandlerService
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.logs.PodLogsWatcher
import com.payu.kube.log.service.pods.PodChangeInterface
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.pods.PodWithAppInterface
import com.payu.kube.log.ui.MainController
import com.payu.kube.log.ui.tab.list.LogEntryCell
import com.payu.kube.log.ui.tab.list.LogListView
import com.payu.kube.log.util.BindingsUtils.mapToObject
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.ClipboardUtils
import com.payu.kube.log.util.LoggerUtils.logger
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import java.net.URL
import java.util.*
import java.util.function.Predicate
import kotlin.concurrent.fixedRateTimer


class TabController(
    monitoredPod: PodInfo,
    private val podStoreService: PodStoreService,
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val stylingTextService: StylingTextService,
    private val mainController: MainController
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
    }

    private fun setupLogList() {
        logListView.stylingTextService = stylingTextService
        logListView.wrapTextProperty.bind(wrapCheckbox.selectedProperty())
        logListView.predicateProperty.bind(searchBox.searchProperty.mapToObject { search ->
            if (search.text.isNotEmpty()) {
                if (search.type == SearchBoxView.SearchType.FILTER) {
                    return@mapToObject Predicate { search.text in it.text }
                } else if (search.type == SearchBoxView.SearchType.NOT_FILTER) {
                    return@mapToObject Predicate { search.text !in it.text }
                }
            }
            return@mapToObject Predicate { true }
        })
        logListView.markedTextProperty.bind(searchBox.searchProperty.mapToString {
            if (it.type == SearchBoxView.SearchType.MARK)
                it.text
            else
                ""
        })
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
                ClipboardUtils.copySelectionToClipboard(logListView)
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
            val newLines = podLogsWatcher?.getNewLogs() ?: return@fixedRateTimer
            updateList(newLines)
        }
    }

    private fun updateList(newElements: List<String>) {
        val styledTexts = newElements.map {
            stylingTextService.styleText(it, LogEntryCell.RULES)
        }
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

    private fun search(search: SearchBoxView.Search) {
        logListView.refresh()
        Platform.runLater {
            Thread.sleep(50)
            if (search.text.isNotEmpty()) {
                val indexToScroll =
                    when (search.type) {
                        SearchBoxView.SearchType.MARK -> {
                            logListView.indexOfLast { search.text in it.text }
                                ?.let {
                                    autoscrollCheckbox.isSelected = false
                                    it
                                }
                        }
                        else -> {
                            autoscrollCheckbox.isSelected = true
                            logListView.indexOfLast()
                        }
                    } ?: return@runLater
                Platform.runLater {
                    logListView.scrollUntilVisible(indexToScroll)
                }
            }
        }
    }

    private fun stopMonitor() {
        podStoreService.stopWatchPod(monitoredPodProperty.value, this)
        podStoreService.stopWatchApp(monitoredPodProperty.value.calculatedAppName, this)
        podLogsWatcher?.stop()
        timer?.cancel()
    }
}

