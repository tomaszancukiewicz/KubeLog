package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import com.payu.kube.log.service.coloring.Rules
import com.payu.kube.log.service.coloring.rules.ColoringQueryRule
import com.payu.kube.log.service.coloring.rules.ColoringRule
import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.ui.tab.list.*

object Code {
    val black: SpanStyle = SpanStyle(Color(0xFF000000))
    val green: SpanStyle = SpanStyle(Color(0xFF2E7D32))
    val yellow: SpanStyle = SpanStyle(Color(0xFFF57F17))
    val red: SpanStyle = SpanStyle(Color(0xFFB71C1C))
    val blue: SpanStyle = SpanStyle(Color(0xFF2979FF))
    val purple: SpanStyle = SpanStyle(Color(0xFF6200EA))
    val gray: SpanStyle = SpanStyle(Color(0xFF616161))
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun Line(item: VirtualItem<String>, query: Query?, onPrevClick: () -> Unit, onAfterClick: () -> Unit, modifier:
Modifier = Modifier) {
    when (item) {
        is Item -> {
            val queryColoringRule by derivedStateOf {
                query?.let { ColoringQueryRule(listOf(), it) }
            }

            val markLine = query?.check(item.value) ?: false

            ContextMenuDataProvider(
                items = {
                    listOf(ContextMenuItem("label") { println("clicked") })
                }
            ) {
                var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                val pressIndicator = Modifier.onPointerEvent(PointerEventType.Press) {
                    val position = it.changes.first().position
                    layoutResult?.let { layoutResult ->
                        println("onPointerEvent " + layoutResult.getOffsetForPosition(position) + " onP" + it.buttons)
                    }
                }

                Text(
                    styleText(item.value, queryColoringRule),
                    fontFamily = FontFamily.Monospace,
                    modifier = if (markLine) pressIndicator.background(Color(0xf8FB98FF)) else pressIndicator,
                    onTextLayout = { layoutResult = it }
                )
            }
        }
        is ShowMoreAfterItem -> {
            DisableSelection {
                Text("Show more after...", modifier = modifier.clickable { onAfterClick() })
            }
        }
        is ShowMoreBeforeItem -> {
            DisableSelection {
                Text("Show more before...", modifier = modifier.clickable { onPrevClick() })
            }
        }
    }
}

private fun styleText(item: String, queryColoringRule: ColoringQueryRule?) = buildAnnotatedString {
    withStyle(Code.black) {
        append(item)

        addStyle(Code.blue, item, Rules.HTTP_METHODS_RULE)
        addStyle(Code.red, item, Rules.ERROR_LOG_LEVEL_RULE)
        addStyle(Code.yellow, item, Rules.WARN_LOG_LEVEL_RULE)
        addStyle(Code.green, item, Rules.INFO_LOG_LEVEL_RULE)
        addStyle(Code.blue, item, Rules.EXTRACT_VALUES_FROM_FIRST_3_BRACKETS_RULE)
        addStyle(Code.purple, item, Rules.EXTRACT_VALUES_FROM_SECOND_BRACKETS_RULE)
        addStyle(Code.gray, item, Rules.EXTRACT_VALUES_FROM_BRACKETS_RULE)
        addStyle(Code.gray, item, Rules.EXTRACT_VALUES_RULE)
        addStyle(Code.gray, item, Rules.IP_RULE)
        addStyle(Code.gray, item, Rules.EMAIL_RULE)
        addStyle(Code.gray, item, Rules.QQ_ID_RULE)
        queryColoringRule?.let { addStyle(Code.red, item, it) }
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, rule: ColoringRule) {
    for (result in rule.findFragments(text)) {
        addStyle(style, result)
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: IntRange) {
    addStyle(style, range.first, range.last + 1)
}