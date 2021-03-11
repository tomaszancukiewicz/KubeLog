package com.payu.kube.log.ui.tab.list

import com.payu.kube.log.service.coloring.StyledText
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.util.ClipboardUtils
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.text.Text
import javafx.util.Callback
import java.util.function.Predicate

class LogListView: ListView<StyledText>() {

    val wrapTextProperty = SimpleBooleanProperty(true)
    val markedTextProperty = SimpleStringProperty("")

    private val logsList = FXCollections.observableArrayList<StyledText>()
    private val filteredLogsList = logsList.filtered { true }

    var stylingTextService: StylingTextService? = null

    val predicateProperty: ObjectProperty<Predicate<in StyledText>>
        get() = filteredLogsList.predicateProperty()

    init {
        skin = CustomListViewSkin(this)
        selectionModel.selectionMode = SelectionMode.MULTIPLE
        cellFactory = Callback {
            val cell = LogEntryCell(stylingTextService!!)
            cell.wrapTextProperty().bind(wrapTextProperty)
            cell.markedTextProperty.bind(markedTextProperty)
            cell
        }

        items = filteredLogsList

        setupLogListContextMenu()
    }

    private fun setupLogListContextMenu() {
        contextMenu = ContextMenu()

        val copyItem = MenuItem("Copy")
        copyItem.setOnAction {
            val value = (it.source as? MenuItem)?.userData as? String ?: return@setOnAction
            ClipboardUtils.setClipboardContent(value)
        }
        contextMenu.items.add(copyItem)

        val copyLineItem = MenuItem("Copy line")
        copyLineItem.setOnAction {
            ClipboardUtils.copySelectionToClipboard(this)
        }
        contextMenu.items.add(copyLineItem)

        val clearBeforeItem = MenuItem("Clear before")
        clearBeforeItem.setOnAction {
            val minSelectionIndex = selectionModel.selectedIndices.firstOrNull() ?: return@setOnAction
            val indexOfOriginalList = filteredLogsList.getSourceIndex(minSelectionIndex)
            logsList.remove(0, indexOfOriginalList)
        }
        contextMenu.items.add(clearBeforeItem)

        setOnContextMenuRequested { contextMenuEvent ->
            val line = selectionModel.selectedItems.firstOrNull() ?: return@setOnContextMenuRequested
            val index = (contextMenuEvent.target as? Text)?.userData as? Int ?: return@setOnContextMenuRequested
            val text = stylingTextService?.calcSegmentForIndex(line.text, LogEntryCell.RULES, index)

            copyItem.isVisible = text != null
            copyItem.userData = text
        }
    }

    fun indexOfLast(predicate: (StyledText) -> Boolean = { true }): Int? {
        return filteredLogsList.indexOfLast(predicate).takeIf { it >= 0 }
    }

    fun scrollToEnd() {
        if (filteredLogsList.size > 0) {
            scrollTo(filteredLogsList.lastIndex)
        }
    }

    fun scrollUntilVisible(indexToScroll: Int) {
        (skin as? CustomListViewSkin<*>)
            ?.forceScrollTo(indexToScroll)
    }

    fun addLines(newElements: List<StyledText>) {
        logsList.addAll(newElements)
    }

    fun clear() {
        logsList.clear()
    }
}