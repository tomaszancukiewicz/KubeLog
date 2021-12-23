package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.payu.kube.log.service.coloring.Rules
import com.payu.kube.log.service.coloring.rules.ColoringQueryRule
import com.payu.kube.log.service.coloring.rules.ColoringRule
import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.util.Item
import com.payu.kube.log.util.ShowMoreAfterItem
import com.payu.kube.log.util.ShowMoreBeforeItem
import com.payu.kube.log.util.VirtualItem

object Code {
    val black: SpanStyle = SpanStyle(Color.Unspecified)
    val green: SpanStyle = SpanStyle(Color(0xFF2E7D32))
    val yellow: SpanStyle = SpanStyle(Color(0xFFF57F17))
    val red: SpanStyle = SpanStyle(Color(0xFFB71C1C))
    val blue: SpanStyle = SpanStyle(Color(0xFF2979FF))
    val purple: SpanStyle = SpanStyle(Color(0xFF6200EA))
    val gray: SpanStyle = SpanStyle(Color(0xFF616161))
    val marked: SpanStyle = red.copy(textDecoration = TextDecoration.Underline)
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun Line(
    item: VirtualItem<String>, query: Query?, onPrevClick: () -> Unit, onAfterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (item) {
        is Item -> {
            val queryColoringRule by derivedStateOf {
                query?.let { ColoringQueryRule(it) }
            }
            val markLine = query?.check(item.value) ?: false

            Text(
                styleText(item.value, queryColoringRule),
                fontFamily = FontFamily.Monospace,
                modifier =
                    if (markLine) Modifier.background(MaterialTheme.colors.secondary.copy(alpha = 0.5f))
                    else Modifier
            )
        }
        is ShowMoreAfterItem -> {
            DisableSelection {
                Text("Show more after...", textAlign = TextAlign.Center, modifier = modifier.clickable { onAfterClick() })
            }
        }
        is ShowMoreBeforeItem -> {
            DisableSelection {
                Text("Show more before...", textAlign = TextAlign.Center, modifier = modifier.clickable { onPrevClick() })
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
        queryColoringRule?.let { addStyle(Code.marked, item, it) }
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