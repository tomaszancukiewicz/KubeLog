package com.payu.kube.log.ui

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.util.Callback
import org.springframework.stereotype.Component
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodListState
import com.payu.kube.log.service.GlobalKeyEventHandlerService
import com.payu.kube.log.service.namespaces.NamespaceStoreService
import com.payu.kube.log.service.pods.PodService
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.service.tab.TabFactoryService
import com.payu.kube.log.util.BindingsUtils.mapToBoolean
import com.payu.kube.log.util.BindingsUtils.mapToObject
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.util.ViewUtils.bindManagedAndVisibility
import javafx.beans.binding.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableObjectValue
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import java.net.URL
import java.util.*
import java.util.function.Predicate
import javafx.scene.control.ToggleGroup
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox

@Component
class MainController(
    private val globalKeyEventHandlerService: GlobalKeyEventHandlerService,
    private val podService: PodService,
    private val podStoreService: PodStoreService,
    private val namespaceStoreService: NamespaceStoreService,
    private val tabFactoryService: TabFactoryService,
    private val searchQueryCompilerService: SearchQueryCompilerService
) : Initializable, EventHandler<KeyEvent> {
    private val log = logger()

    companion object {
        private val TOGGLE_POD_LIST_CODE_COMBINATION = KeyCodeCombination(KeyCode.T, KeyCodeCombination.SHORTCUT_DOWN)

        private val CLEAR_SEARCH_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ESCAPE)
        private val OPEN_TAB_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ENTER)
    }

    @FXML
    lateinit var menuBar: MenuBar

    @FXML
    lateinit var menu: Menu

    @FXML
    lateinit var splitPane: SplitPane

    @FXML
    lateinit var podsListContainer: VBox

    @FXML
    lateinit var tabPane: TabPane

    @FXML
    lateinit var statusPanel: Parent

    @FXML
    lateinit var statusLabel: Label

    @FXML
    lateinit var reloadButton: Button

    @FXML
    lateinit var podsListPanel: Parent

    @FXML
    lateinit var searchTextField: TextField

    @FXML
    lateinit var listView: ListView<PodInfo>

    private val filteredList = podStoreService.podsSorted.filtered { true }

    private val toggleGroup = ToggleGroup()

    private val podsVisible = SimpleBooleanProperty(true)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        log.info("initialize")
        globalKeyEventHandlerService.registerKeyPressEventHandler(this)
        menuBar.isUseSystemMenuBar = isMacOs()
        initMenuList()

        initSplitPane()

        statusLabel.textProperty().bind(podStoreService.status.podListStateToString())
        statusLabel.bindManagedAndVisibility(statusLabel.textProperty().isNotEmpty)
        reloadButton.bindManagedAndVisibility(
            podStoreService.status.podListStateIsError().and(namespaceStoreService.currentNamespace.isNotNull)
        )
        reloadButton.setOnAction {
            val currentNamespace = namespaceStoreService.currentNamespace.get()
            currentNamespace?.let {
                podService.startMonitorNamespace(it)
            }
        }
        val showStatusPanel = reloadButton.visibleProperty().or(statusLabel.visibleProperty())
        statusPanel.bindManagedAndVisibility(showStatusPanel)
        podsListPanel.bindManagedAndVisibility(showStatusPanel.not())

        tabPane.bindManagedAndVisibility(Bindings.isNotEmpty(tabPane.tabs))
        podsListContainer.bindManagedAndVisibility(podsVisible.or(showStatusPanel).or(tabPane.visibleProperty().not()))

        searchTextField.setOnKeyPressed {
            if (CLEAR_SEARCH_KEY_CODE_COMBINATION.match(it)) {
                searchTextField.text = ""
                it.consume()
            }
        }
        filteredList.predicateProperty().bind(searchTextField.textProperty().mapToObject { text ->
            text?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { searchQueryCompilerService.compile(it) }
                ?.let { Predicate { pod -> it.check(pod.name) } }
                ?: Predicate { true }
        })

        listView.items = filteredList
        listView.cellFactory = Callback { PodInfoViewCell() }
        listView.setOnMouseClicked {
            openSelectedPod()
            listView.selectionModel.clearSelection()
        }
        listView.setOnKeyPressed {
            if (OPEN_TAB_KEY_CODE_COMBINATION.match(it)) {
                openSelectedPod()
                it.consume()
            }
        }
    }

    private fun initSplitPane() {
        val setSplitPaneCallback = {
            val splitPaneItems = mutableListOf<Node>().apply {
                if (podsListContainer.visibleProperty().value) {
                    add(podsListContainer)
                }
                if (tabPane.visibleProperty().value) {
                    add(tabPane)
                }
            }
            splitPane.items.setAll(splitPaneItems)
        }
        podsListContainer.visibleProperty().addListener { _, _, _ ->
            setSplitPaneCallback()
        }
        tabPane.visibleProperty().addListener { _, _, _ ->
            setSplitPaneCallback()
        }
        setSplitPaneCallback()
    }

    private fun openSelectedPod() {
        val selectedPod = listView.selectionModel.selectedItem ?: return
        openPod(selectedPod)
    }

    fun openPod(selectedPod: PodInfo) {
        val newTab = tabFactoryService.createTab(selectedPod, this)
        tabPane.tabs.add(newTab)
        tabPane.selectionModel.select(newTab)
    }

    private fun initMenuList() {
        namespaceStoreService.allNamespacesSorted.addListener(ListChangeListener {
            buildMenu(namespaceStoreService.allNamespacesSorted, namespaceStoreService.currentNamespace.get())
        })
        namespaceStoreService.currentNamespace.addListener { _, _, _ ->
            val currentNamespace = namespaceStoreService.currentNamespace.get()
            buildMenu(namespaceStoreService.allNamespacesSorted, currentNamespace)
            podService.startMonitorNamespace(currentNamespace)
        }
        val currentNamespace = namespaceStoreService.currentNamespace.get()
        buildMenu(namespaceStoreService.allNamespacesSorted, currentNamespace)
        currentNamespace?.let {
            podService.startMonitorNamespace(it)
        }

        toggleGroup.selectedToggleProperty().addListener { _, _, _ ->
            val data = toggleGroup.selectedToggle?.userData as? String ?: return@addListener
            log.info("select new namespace: $data")
            namespaceStoreService.currentNamespace.set(data)
        }
    }

    private fun buildMenu(list: List<String>, selected: String?) {
        val radioItems = list.map {
            val radioMenuItem = RadioMenuItem(it)
            radioMenuItem.toggleGroup = toggleGroup
            radioMenuItem.isSelected = it == selected
            radioMenuItem.userData = it
            radioMenuItem
        }
        menu.items.setAll(radioItems)
    }

    private fun isMacOs(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("mac")
    }

    override fun handle(event: KeyEvent) {
        if (TOGGLE_POD_LIST_CODE_COMBINATION.match(event)) {
            podsVisible.value = !podsVisible.value
            event.consume()
        }
    }

    private fun ObservableObjectValue<PodListState?>.podListStateToString(): StringBinding {
        return this.mapToString {
            when (it) {
                PodListState.LoadingPods -> "Loading"
                PodListState.Data -> ""
                is PodListState.ErrorPods -> "Error ${it.message}"
                else -> ""
            }
        }
    }

    private fun ObservableObjectValue<PodListState?>.podListStateIsError(): BooleanBinding {
        return this.mapToBoolean {
            val value = it ?: return@mapToBoolean false
            return@mapToBoolean value is PodListState.ErrorPods
        }
    }
}

