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
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.ui.compose.component.Select
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.ThemeProvider

enum class SearchType {
    FILTER, MARK,
}

class SearchState {
    var isVisible by mutableStateOf(false)
    var text by mutableStateOf("")
    var searchType by mutableStateOf(SearchType.FILTER)

    val query = derivedStateOf {
        text.takeIf { it.isNotEmpty() && isVisible }
            ?.let { SearchQueryCompilerService.compile(it) }
    }

    fun toggleVisible() {
        isVisible = !isVisible
    }
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun SearchView(search: SearchState, onSearchRequest: () -> Unit) {
    val query by search.query
    val queryErrors by remember { derivedStateOf { query?.errors } }
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(search.isVisible) {
        if (search.isVisible) {
            textFieldFocusRequester.requestFocus()
        }
    }

    if (search.isVisible) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            TextField(
                value = search.text,
                onValueChange = { search.text = it },
                modifier = Modifier.weight(1.0f)
                    .focusRequester(textFieldFocusRequester)
                    .onKeyEvent {
                        if (it.type != KeyEventType.KeyDown) {
                            return@onKeyEvent false
                        }
                        when (it.key) {
                            Key.Escape -> {
                                search.text = ""
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
                SearchType.values().toList(),
                search.searchType, { search.searchType = it },
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
        SearchView(SearchState().apply { isVisible = true }) {}
    }
}