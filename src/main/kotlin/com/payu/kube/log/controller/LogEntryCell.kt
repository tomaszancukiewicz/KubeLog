package com.payu.kube.log.controller

import com.payu.kube.log.service.coloring.ColoringRule
import com.payu.kube.log.service.coloring.StylingTextService
import com.payu.kube.log.util.ViewUtils.toggleClass
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.text.Text
import javafx.scene.text.TextFlow


class LogEntryCell(
    private val stylingTextService: StylingTextService,
    private val isWrappingProperty: ObservableValue<Boolean>,
    private val searchedText: ObservableValue<String>
) : ListCell<String?>() {

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

        private val HTTP_METHODS_RULE = ColoringRule.ColoringRegexRule(
            COLORED_BLUE_TEXT_CLASS,
            "(?:GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH)".toRegex()
        )
        private val ERROR_LOG_LEVEL_RULE = ColoringRule.ColoringRegexRule(
            COLORED_RED_TEXT_CLASS,
            "(?:FATAL|ERROR)".toRegex()
        )
        private val WARN_LOG_LEVEL_RULE = ColoringRule.ColoringRegexRule(
            COLORED_YELLOW_TEXT_CLASS,
            "(?:WARN)".toRegex()
        )
        private val INFO_LOG_LEVEL_RULE = ColoringRule.ColoringRegexRule(
            COLORED_GREEN_TEXT_CLASS,
            "(?:INFO|DEBUG|TRACE)".toRegex()
        )
        private val IP_RULE = ColoringRule.ColoringRegexRule(
            COLORED_GRAY_TEXT_CLASS,
            "(?:25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d{1,2})){3}".toRegex()
        )
        private val QQ_ID_RULE = ColoringRule.ColoringRegexRule(
            COLORED_GRAY_TEXT_CLASS,
            "(?:QQ[0-9A-Z]{16}QQ|TT[0-9]{10}TT)".toRegex()
        )

        val RULES = listOf(
            HTTP_METHODS_RULE,
            ERROR_LOG_LEVEL_RULE,
            WARN_LOG_LEVEL_RULE,
            INFO_LOG_LEVEL_RULE,
            IP_RULE,
            QQ_ID_RULE
        )
    }

    private val textFlow = TextFlow()

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        text = null
        if (graphic == null) {
            graphic = textFlow
        }

        minWidth = 0.0
        if (isWrappingProperty.value) {
            maxWidthProperty().bind(listView.widthProperty().subtract(20))
            prefWidthProperty().bind(listView.widthProperty().subtract(20))
        } else {
            maxWidthProperty().unbind()
            prefWidthProperty().unbind()
            prefWidth = USE_COMPUTED_SIZE
            maxWidth = Double.MAX_VALUE
        }

        toggleClass(LOG_ENTRY_CLASS, true)
        val styleText = stylingTextService.styleText(
            item ?: "",
            RULES + ColoringRule.ColoringTextRule(MARKED_SEARCHED_TEXT_CLASS, searchedText.value)
        )
        toggleClass(SEARCHED_LOG_ENTRY_CLASS, MARKED_SEARCHED_TEXT_CLASS in styleText.appliedStyles)
        val textNodes = createTextFlowNodes(styleText)
        textFlow.children.setAll(textNodes)
    }

    private fun createTextFlowNodes(styledText: StylingTextService.StyledText): List<Node> {
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