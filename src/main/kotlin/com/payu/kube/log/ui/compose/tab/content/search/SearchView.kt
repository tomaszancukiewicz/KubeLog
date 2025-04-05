package com.payu.kube.log.ui.compose.tab.content.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.Select
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

@Composable
fun SearchView(search: SearchState, onSearchRequest: () -> Unit) {
    val queryErrors by search.queryErrors
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
                    CompileQueryIndicator(queryErrors)
                }
            )
            Spacer(Modifier.width(8.dp))
            Select(
                SearchType.entries,
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