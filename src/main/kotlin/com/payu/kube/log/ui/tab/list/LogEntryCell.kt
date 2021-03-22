package com.payu.kube.log.ui.tab.list

import com.payu.kube.log.service.coloring.StyledText
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.service.coloring.rules.ColoringRegexRule
import com.payu.kube.log.service.coloring.rules.ColoringQueryRule
import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.util.ClipboardUtils
import com.payu.kube.log.util.RegexUtils.getOrNull
import com.payu.kube.log.util.ViewUtils.toggleClass
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
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
        private const val COLORED_GREEN_TEXT_CLASS = "color-background-green"
        private const val COLORED_YELLOW_TEXT_CLASS = "color-background-yellow"
        private const val COLORED_RED_TEXT_CLASS = "color-background-red"
        private const val COLORED_BLUE_TEXT_CLASS = "color-background-blue"
        private const val COLORED_GRAY_TEXT_CLASS = "color-background-gray"
        private const val COLORED_PURPLE_TEXT_CLASS = "color-background-purple"

        private val HTTP_METHODS_RULE = ColoringRegexRule(
            listOf(COLORED_BLUE_TEXT_CLASS, "http-method"),
            "GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH".toRegex()
        )
        private val ERROR_LOG_LEVEL_RULE = object : ColoringRegexRule(
            listOf(COLORED_RED_TEXT_CLASS, "log-level"),
            "^.*?(?:(FATAL|ERROR)|WARN|INFO|DEBUG|TRACE)".toRegex()
        ) {
            override fun extractRange(matchResult: MatchResult): IntRange? {
                return matchResult.groups
                    .getOrNull(1)
                    ?.range
            }
        }
        private val WARN_LOG_LEVEL_RULE = object : ColoringRegexRule(
            listOf(COLORED_YELLOW_TEXT_CLASS, "log-level"),
            "^.*?(?:FATAL|ERROR|(WARN)|INFO|DEBUG|TRACE)".toRegex()
        ) {
            override fun extractRange(matchResult: MatchResult): IntRange? {
                return matchResult.groups
                    .getOrNull(1)
                    ?.range
            }
        }
        private val INFO_LOG_LEVEL_RULE = object : ColoringRegexRule(
            listOf(COLORED_GREEN_TEXT_CLASS, "log-level"),
            "^.*?(?:FATAL|ERROR|WARN|(INFO|DEBUG|TRACE))".toRegex()
        ) {
            override fun extractRange(matchResult: MatchResult): IntRange? {
                return matchResult.groups
                    .getOrNull(1)
                    ?.range
            }
        }
        private val EXTRACT_VALUES_FROM_FIRST_3_BRACKETS_RULE = object : ColoringRegexRule(
            listOf(COLORED_BLUE_TEXT_CLASS, "brackets"),
            ".*?(?:\\[([^\\[\\]]*)\\]|\\(([^()]*)\\))".toRegex()
        ) {
            override fun findFragments(text: String): List<IntRange> {
                return super.findFragments(text).take(3)
            }
        }
        private val EXTRACT_VALUES_FROM_SECOND_BRACKETS_RULE = object : ColoringRegexRule(
            listOf(COLORED_PURPLE_TEXT_CLASS, "brackets"),
            ".*?(?:\\[([^\\[\\]]*)\\]|\\(([^()]*)\\))".toRegex()
        ) {
            override fun findFragments(text: String): List<IntRange> {
                return super.findFragments(text)
                    .getOrNull(1)
                    ?.let { listOf(it) }
                    ?: listOf()
            }
        }
        private val EXTRACT_VALUES_FROM_BRACKETS_RULE = ColoringRegexRule(
            listOf("brackets"),
            "\\[([^\\[\\]]*)\\]|\\(([^()]*)\\)|<([^<>]*)>".toRegex()
        )
        private val EXTRACT_VALUES_RULE = ColoringRegexRule(
            listOf(COLORED_GRAY_TEXT_CLASS, "value"),
            "(?<=[,\\s(\\[{])[\\w]+=([\\w+:./?#@-]+)(?=[,\\s)\\]}]|$)".toRegex()
        )
        private val IP_RULE = ColoringRegexRule(
            listOf(COLORED_GRAY_TEXT_CLASS, "ip"),
            "(?:25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})){3}".toRegex()
        )
        private val EMAIL_RULE = ColoringRegexRule(
            listOf(COLORED_GRAY_TEXT_CLASS, "email"),
            "(?:[\\w]+(?:[.+-][\\w]+)*)@(?:(?:[\\w-]+\\.)*\\w[\\w-]{0,66})\\.(?:[a-z]{2,6}(?:\\.[a-z]{2})?)".toRegex()
        )
        private val QQ_ID_RULE = ColoringRegexRule(
            listOf(COLORED_GRAY_TEXT_CLASS, "qq-id"),
            "QQ[0-9A-Z]{16}QQ|TT[0-9]{10}TT".toRegex()
        )

        val RULES = listOf(
            HTTP_METHODS_RULE,
            ERROR_LOG_LEVEL_RULE,
            WARN_LOG_LEVEL_RULE,
            INFO_LOG_LEVEL_RULE,
            EXTRACT_VALUES_FROM_FIRST_3_BRACKETS_RULE,
            EXTRACT_VALUES_FROM_SECOND_BRACKETS_RULE,
            EXTRACT_VALUES_FROM_BRACKETS_RULE,
            EXTRACT_VALUES_RULE,
            IP_RULE,
            EMAIL_RULE,
            QQ_ID_RULE
        )
    }

    val markQueryProperty = SimpleObjectProperty<Query?>(null)

    private val textFlow = TextFlow()

    var onClearBefore: ((Int) -> Unit)? = null
    var onCopySelected: (() -> Unit)? = null

    init {
        toggleClass(LOG_ENTRY_CLASS, true)
        setupLogCellContextMenu()
    }

    private fun setupLogCellContextMenu() {
        contextMenu = ContextMenu()

        val clearBeforeItem = MenuItem("Clear before")
        clearBeforeItem.setOnAction {
            onClearBefore?.invoke(index)
        }
        contextMenu.items.add(clearBeforeItem)

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

        setOnContextMenuRequested { contextMenuEvent ->
            val item = item as? Item
            if (item == null) {
                clearBeforeItem.isVisible = false
                copyItem.isVisible = false
                return@setOnContextMenuRequested
            }
            clearBeforeItem.isVisible = true
            val inTextIndex = (contextMenuEvent.target as? Text)?.userData as? Int
            if (inTextIndex == null) {
                copyItem.isVisible = false
                return@setOnContextMenuRequested
            }
            val textToCopy = stylingTextService.calcSegmentForIndex(item.value.text, RULES, inTextIndex)

            copyItem.isVisible = textToCopy != null
            copyItem.userData = textToCopy
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