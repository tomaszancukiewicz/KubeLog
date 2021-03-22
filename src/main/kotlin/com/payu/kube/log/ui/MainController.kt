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
import com.payu.kube.log.service.namespaces.NamespaceStoreService
import com.payu.kube.log.service.pods.PodService
import com.payu.kube.log.service.pods.PodStoreService
import com.payu.kube.log.service.tab.TabFactoryService
import com.payu.kube.log.util.BindingsUtils.mapToBoolean
import com.payu.kube.log.util.BindingsUtils.mapToObject
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.LoggerUtils.logger
import com.payu.kube.log.util.ViewUtils.bindManagedAndVisibility
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableObjectValue
import javafx.collections.ListChangeListener
import javafx.scene.Parent
import java.net.URL
import java.util.*
import java.util.function.Predicate
import javafx.scene.control.ToggleGroup

@Component
class MainController(
    private val podService: PodService,
    private val podStoreService: PodStoreService,
    private val namespaceStoreService: NamespaceStoreService,
    private val tabFactoryService: TabFactoryService
) : Initializable {
    private val log = logger()

    companion object {
        private val CLEAR_SEARCH_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ESCAPE)
        private val OPEN_TAB_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ENTER)
    }

    @FXML
    lateinit var menuBar: MenuBar

    @FXML
    lateinit var menu: Menu

    @FXML
    lateinit var statusPanel: Parent

    @FXML
    lateinit var statusLabel: Label

    @FXML
    lateinit var reloadButton: Button

    @FXML
    lateinit var logsPanel: Parent

    @FXML
    lateinit var searchTextField: TextField

    @FXML
    lateinit var listView: ListView<PodInfo>

    @FXML
    lateinit var tabPane: TabPane

    private val filteredList = podStoreService.podsSorted.filtered { true }

    private val toggleGroup = ToggleGroup()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        log.info("initialize")
        menuBar.isUseSystemMenuBar = isMacOs()
        initMenuList()
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
        logsPanel.bindManagedAndVisibility(showStatusPanel.not())

        searchTextField.setOnKeyPressed {
            if (CLEAR_SEARCH_KEY_CODE_COMBINATION.match(it)) {
                searchTextField.text = ""
                it.consume()
            }
        }
        filteredList.predicateProperty().bind(searchTextField.textProperty().mapToObject {
            val searchText = it?.trim() ?: ""
            if (searchText.isEmpty())
                Predicate { true }
            else
                Predicate { pod -> searchText in pod.name }
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

