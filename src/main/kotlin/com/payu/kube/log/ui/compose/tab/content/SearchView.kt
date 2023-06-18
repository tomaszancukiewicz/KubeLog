package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.service.search.SearchQueryCompilerService
import com.payu.kube.log.ui.compose.component.Select
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.theme.LocalCustomColorScheme
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchView(search: SearchState, onSearchRequest: () -> Unit) {
    val customColorScheme = LocalCustomColorScheme.current
    val query by search.query
    val queryErrors by remember { derivedStateOf { query?.errors } }
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(search.isVisible) {
        if (search.isVisible) {
            textFieldFocusRequester.requestFocus()
        }
    }

    if (search.isVisible) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
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
                placeholder = "Search in logs e.g. \"Hello\" OR \"world\"",
                trailingIcon = {
                    TooltipArea(
                        tooltip = {
                            ElevatedCard {
                                Text(
                                    text = queryErrors
                                        ?.takeIf { it.isNotEmpty() }
                                        ?.joinToString("\n", "Literal mode.\nNot interpreted because of errors:\n") {
                                            " - $it"
                                        } ?: "Interpreted mode",
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        },
                        delayMillis = 0,
                        tooltipPlacement = TooltipPlacement.CursorPoint()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (queryErrors.isNullOrEmpty()) customColorScheme.blue
                                    else customColorScheme.green,
                                    CircleShape
                                )
                        )
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            Select(
                SearchType.values().toList(),
                search.searchType, { search.searchType = it }
            )
        }
    }
}

@Preview
@Composable
private fun SearchViewPreview() {
    ThemeProvider {
        SearchView(SearchState().apply { isVisible = true }) {}
    }
}