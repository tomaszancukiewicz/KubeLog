package com.payu.kube.log.ui.tab.list

import com.payu.kube.log.service.coloring.StyledText
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.ui.tab.SearchBoxView
import com.payu.kube.log.util.BindingsUtils.mapToObject
import com.payu.kube.log.util.ClipboardUtils
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseButton
import javafx.util.Callback
import java.util.function.Predicate
import kotlin.math.max
import kotlin.math.min

class LogListView: ListView<VirtualItem>() {

    companion object {
        private const val MORE_ELEMENT = 3

        private val ACTION_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ENTER)
    }

    val wrapTextProperty = SimpleBooleanProperty(true)
    val searchProperty = SimpleObjectProperty<SearchBoxView.Search?>(null)

    private val markedTextProperty = searchProperty.mapToObject { it?.query }
    private val predicateProperty: ObjectBinding<Predicate<in StyledText>> = searchProperty.mapToObject { search ->
        if (search?.type == SearchBoxView.SearchType.FILTER) {
            return@mapToObject Predicate { search.query.check(it.text) }
        }
        return@mapToObject Predicate { true }
    }

    private val allLogsList = FXCollections.observableArrayList<StyledText>()
    private val showedLogList = FXCollections.observableArrayList<VirtualItem>()

    var stylingTextService: StylingTextService? = null

    init {
        skin = CustomListViewSkin(this)
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        cellFactory = Callback {
            val cell = LogEntryCell(stylingTextService!!)
            cell.wrapTextProperty().bind(wrapTextProperty)
            cell.markQueryProperty.bind(markedTextProperty)
            cell.onClearBefore = this::clearToIndex
            cell.onCopySelected = this::copySelectionToClipboard
            cell
        }

        items = showedLogList

        recalculate()
        predicateProperty.addListener { _, _, _ ->
            recalculate()
        }

        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY && !it.isShiftDown) {
                onCellAction()
                it.consume()
            }
        }
        setOnKeyPressed {
            if (ACTION_KEY_CODE_COMBINATION.match(it)) {
                onCellAction()
                it.consume()
            }
        }
    }

    fun indexOfLast(predicate: (StyledText) -> Boolean = { true }): Int? {
        return showedLogList.indexOfLast {
            (it as? Item)?.let { item -> predicate.invoke(item.value) } ?: false
        }.takeIf { it >= 0 }
    }

    fun scrollToEnd() {
        if (showedLogList.size > 0) {
            scrollTo(showedLogList.lastIndex)
        }
    }

    fun scrollUntilVisible(indexToScroll: Int) {
        (skin as? CustomListViewSkin<*>)
            ?.forceScrollTo(indexToScroll)
    }

    fun clear() {
        allLogsList.clear()
        showedLogList.clear()
    }

    private fun clearToIndex(cellIndex: Int) {
        val indexOfOriginalList = showedLogList[cellIndex]
            .takeIf { it is Item }
            ?.originalIndex ?: return
        allLogsList.remove(0, indexOfOriginalList)
        showedLogList.remove(0, cellIndex)
        showedLogList.forEach {
            it.originalIndex -= indexOfOriginalList - 1
        }
    }

    fun copySelectionToClipboard() {
        val clipboardString = StringBuilder()
        val selectedItems = selectionModel.selectedItems ?: listOf()
        for (item in selectedItems) {
            if (item !is Item) continue
            clipboardString.append(item.value.text)
            clipboardString.append("\n")
        }
        ClipboardUtils.setClipboardContent(clipboardString.toString())
    }

    private fun recalculate() {
        val newShowedList = mutableListOf<VirtualItem>()
        var areAnyElementsBefore = false
        for (i in allLogsList.indices) {
            val value = allLogsList[i]
            val isShowed = predicateProperty.value.test(value)
            if (isShowed) {
                val lastElement = newShowedList.lastOrNull()
                val item = Item(value, i)
                if (areAnyElementsBefore) {
                    if (lastElement is Item) {
                        newShowedList.add(ShowMoreAfterItem(lastElement))
                    }
                    newShowedList.add(ShowMoreBeforeItem(item))
                }
                newShowedList.add(item)
            }
            areAnyElementsBefore = !isShowed
        }

        newShowedList.lastOrNull()
            ?.takeIf { areAnyElementsBefore && it is Item }
            ?.let { newShowedList.add(ShowMoreAfterItem(it as Item)) }

        showedLogList.setAll(newShowedList)
    }

    fun addLines(newElements: List<StyledText>) {
        newElements.forEach {
            addLine(it)
        }
    }

    private fun addLine(value: StyledText) {
        allLogsList.add(value)
        val item = Item(value, allLogsList.lastIndex)
        val lastElement = showedLogList.lastOrNull()
        if (predicateProperty.value.test(value)) {
            if (lastElement is ShowMoreAfterItem) {
                if (lastElement.originalIndex + 1 == item.originalIndex) {
                    showedLogList[showedLogList.lastIndex] = item
                } else {
                    showedLogList.add(ShowMoreBeforeItem(item))
                    showedLogList.add(item)
                }
            } else {
                showedLogList.add(item)
            }
        } else {
            if (lastElement is Item) {
                showedLogList.add(ShowMoreAfterItem(lastElement))
            }
        }
    }

    private fun onCellAction() {
        val index = selectionModel.selectedIndex
        val item = showedLogList.getOrNull(index) ?: return
        when (item) {
            is ShowMoreBeforeItem -> showBefore(index, item)
            is ShowMoreAfterItem -> showAfter(index, item)
            else -> return
        }
    }

    private fun showBefore(cellIndex: Int, item: ShowMoreBeforeItem) {
        var prevItem: Item? = null
        var actualIndex = cellIndex + 1
        for (i in cellIndex - 1 downTo 0) {
            prevItem = showedLogList[i] as? Item ?: continue
            break
        }

        val minAddedElement = max((prevItem?.originalIndex ?: -1) + 1, item.originalIndex - MORE_ELEMENT)
        var lastAddedItem: Item? = null
        for (i in item.originalIndex - 1 downTo minAddedElement) {
            val newItem = Item(allLogsList[i], i)
            showedLogList.add(actualIndex, newItem)
            lastAddedItem = newItem
        }

        lastAddedItem ?: return
        actualIndex--
        showedLogList[actualIndex] = ShowMoreBeforeItem(lastAddedItem)

        if (lastAddedItem.originalIndex == 0 ||
            lastAddedItem.originalIndex - 1 == prevItem?.originalIndex) {
            showedLogList.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreBeforeItem }
                ?.let { showedLogList.removeAt(actualIndex) }
            actualIndex--

            showedLogList.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreAfterItem }
                ?.let { showedLogList.removeAt(actualIndex) }
        }
    }

    private fun showAfter(cellIndex: Int, item: ShowMoreAfterItem) {
        var nextItem: Item? = null
        var actualIndex = cellIndex
        for (i in cellIndex + 1 until showedLogList.size) {
            nextItem = showedLogList[i] as? Item ?: continue
            break
        }

        val maxAddedElement = min(nextItem?.originalIndex ?: allLogsList.size, item.originalIndex + 1 + MORE_ELEMENT)
        var lastAddedItem: Item? = null
        for (i in item.originalIndex + 1 until maxAddedElement) {
            val newItem = Item(allLogsList[i], i)
            showedLogList.add(actualIndex, newItem)
            actualIndex++
            lastAddedItem = newItem
        }

        lastAddedItem ?: return
        showedLogList[actualIndex] = ShowMoreAfterItem(lastAddedItem)

        if (lastAddedItem.originalIndex == allLogsList.lastIndex ||
            lastAddedItem.originalIndex + 1 == nextItem?.originalIndex) {
            showedLogList.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreAfterItem }
                ?.let { showedLogList.removeAt(actualIndex) }

            showedLogList.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreBeforeItem }
                ?.let { showedLogList.removeAt(actualIndex) }
        }
    }
}