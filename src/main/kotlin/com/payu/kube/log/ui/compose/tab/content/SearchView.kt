package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.Select
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.ThemeProvider
import com.payu.kube.log.ui.compose.tab.SearchState
import com.payu.kube.log.ui.compose.tab.SearchType

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun SearchView(search: SearchState, onSearchRequest: () -> Unit) {
    val isVisible by search.isVisible
    var searchText by search.text
    var searchType by search.searchType
    val query by search.query
    val queryErrors by remember { derivedStateOf { query?.errors } }
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            textFieldFocusRequester.requestFocus()
        }
    }

    if (isVisible) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.weight(1.0f)
                    .focusRequester(textFieldFocusRequester)
                    .onKeyEvent {
                        if (it.type != KeyEventType.KeyDown) {
                            return@onKeyEvent false
                        }
                        when (it.key) {
                            Key.Escape -> {
                                searchText = ""
                                true
                            }
                            Key.Enter -> {
                                onSearchRequest()
                                true
                            }
                            else -> false
                        }
                    },
                placeholder = "Search in logs e.g. \"Hello\" OR \"world\""
            )
            TooltipArea(
                tooltip = {
                    Surface(
                        modifier = Modifier.shadow(4.dp),
                        color = Color(255, 255, 210),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = queryErrors
                                ?.takeIf { it.isNotEmpty() }
                                ?.joinToString("\n", "Literal mode.\nNot interpreted because of errors:\n") {
                                    " - $it"
                                } ?: "Interpreted mode",
                            color = Color.Black,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                },
                delayMillis = 0,
                tooltipPlacement = TooltipPlacement.CursorPoint(alignment = Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier.width(20.dp).fillMaxHeight()
                        .background(if (queryErrors.isNullOrEmpty()) Color(0xFF2979FF) else Color(0xFF2bc140))
                )
            }
            Select(
                listOf(SearchType.FILTER, SearchType.MARK),
                searchType, { searchType = it },
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Preview
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
private fun SearchViewPreview() {
    ThemeProvider {
        SearchView(SearchState().apply { isVisible.value = true }) {}
    }
}