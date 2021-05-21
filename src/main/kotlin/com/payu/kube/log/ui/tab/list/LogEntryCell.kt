package com.payu.kube.log.ui.tab.list

import com.payu.kube.log.service.coloring.Rules
import com.payu.kube.log.service.coloring.StyledText
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.coloring.rules.ColoringQueryRule
import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.ui.tab.SearchBoxView
import com.payu.kube.log.util.ClipboardUtils
import com.payu.kube.log.util.ViewUtils.toggleClass
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.scene.text.TextFlow


class LogEntryCell(private val stylingTextService: StylingTextService) : ListCell<VirtualItem?>() {

    companion object {
        private const val LOG_ENTRY_CLASS = "log-entry"
        private const val SEARCHED_LOG_ENTRY_CLASS = "searched"
        private const val TEXT_CLASS = "text"
        private const val MARKED_SEARCHED_TEXT_CLASS = "marked"
    }

    val markQueryProperty = SimpleObjectProperty<Query?>(null)

    private val textFlow = TextFlow()

    var onClearBefore: ((Int) -> Unit)? = null
    var onCopySelected: (() -> Unit)? = null
    var onAddToSearch: ((SearchBoxView.AddToSearchType, String) -> Unit)? = null

    init {
        toggleClass(LOG_ENTRY_CLASS, true)
        setupLogCellContextMenu()
    }

    private fun setupLogCellContextMenu() {
        contextMenu = ContextMenu()

        val addToSearchMenu = Menu("Add to search")
        for (type in SearchBoxView.AddToSearchType.values()) {
            val addToSearchItem = MenuItem(type.name)
            addToSearchItem.setOnAction {
                val value = (it.source as? MenuItem)?.parentMenu?.userData as? String ?: return@setOnAction
                onAddToSearch?.invoke(type, value)
            }
            addToSearchMenu.items.add(addToSearchItem)
        }
        contextMenu.items.add(addToSearchMenu)

        val copyItem = MenuItem("Copy")
        copyItem.setOnAction {
            val value = (it.source as? MenuItem)?.userData as? String ?: return@setOnAction
            ClipboardUtils.setClipboardContent(value)
        }
        contextMenu.items.add(copyItem)

        val copyLineItem = MenuItem("Copy line")
        copyLineItem.setOnAction {
            onCopySelected?.invoke()
        }
        contextMenu.items.add(copyLineItem)

        val clearBeforeItem = MenuItem("Clear before")
        clearBeforeItem.setOnAction {
            onClearBefore?.invoke(index)
        }
        contextMenu.items.add(clearBeforeItem)

        setOnContextMenuRequested { contextMenuEvent ->
            val item = item as? Item
            if (item == null) {
                clearBeforeItem.isVisible = false
                copyItem.isVisible = false
                addToSearchMenu.isVisible = false
                return@setOnContextMenuRequested
            }
            clearBeforeItem.isVisible = true
            val inTextIndex = (contextMenuEvent.target as? Text)?.userData as? Int
            if (inTextIndex == null) {
                copyItem.isVisible = false
                addToSearchMenu.isVisible = false
                return@setOnContextMenuRequested
            }
            val textToCopy = stylingTextService.calcSegmentForIndex(item.value.text, Rules.RULES, inTextIndex)

            copyItem.isVisible = textToCopy != null
            copyItem.userData = textToCopy
            addToSearchMenu.isVisible = textToCopy != null
            addToSearchMenu.userData = textToCopy
        }
    }

    override fun updateItem(item: VirtualItem?, empty: Boolean) {
        super.updateItem(item, empty)
        toggleClass(SEARCHED_LOG_ENTRY_CLASS, false)
        text = null
        graphic = null
        if (empty || item == null) {
            return
        }

        when(item) {
            is Item -> {
                if (isWrapText) {
                    textFlow.prefWidth = Region.USE_PREF_SIZE
                } else {
                    textFlow.prefWidth = Region.USE_COMPUTED_SIZE
                }

                val markLine = markQueryProperty.value?.check(item.value.text) ?: false
                val styleText = markQueryProperty.value?.let {
                    stylingTextService.styleText(
                        item.value,
                        listOf(ColoringQueryRule(listOf(MARKED_SEARCHED_TEXT_CLASS), it))
                    )
                } ?: item.value
                toggleClass(SEARCHED_LOG_ENTRY_CLASS,  markLine)
                val textNodes = createTextFlowNodes(styleText)
                textFlow.children.setAll(textNodes)

                alignment = Pos.CENTER_LEFT
                graphic = textFlow
            }
            is ShowMoreAfterItem -> {
                text = "Show more after..."
                alignment = Pos.CENTER
            }
            is ShowMoreBeforeItem -> {
                text = "Show more before..."
                alignment = Pos.CENTER
            }
        }
    }

    private fun createTextFlowNodes(styledText: StyledText): List<Node> {
        val segments = styledText.createSegments()
        val result = mutableListOf<Text>()
        var index = 0
        for ((text, styles) in segments) {
            val textNode = Text(text)
            textNode.toggleClass(TEXT_CLASS, true)
            for (style in styles) {
                textNode.toggleClass(style, true)
            }
            textNode.userData = index
            result.add(textNode)
            index += text.length
        }
        return result
    }
}