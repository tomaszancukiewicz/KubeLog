package com.payu.kube.log.ui.compose.tab.content.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import com.payu.kube.log.ui.compose.component.TextField
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

@Composable
fun SearchView(
    search: SearchState
) {
    val queryErrors by search.queryErrors
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(search.isVisible) {
        if (search.isVisible) {
            textFieldFocusRequester.requestFocus()
        }
    }

    if (!search.isVisible) {
        return
    }

    TextField(
        value = search.text,
        onValueChange = { search.text = it },
        modifier = Modifier
            .fillMaxWidth()
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

                    else -> false
                }
            },
        placeholder = "Search in logs e.g. \"Hello\" OR \"world\"",
        leadingIcon = {
            SearchTypePicker(
                value = search.searchType,
                onValueChange = { search.searchType = it }
            )
        },
        trailingIcon = {
            CompileQueryIndicator(queryErrors)
        }
    )
}

@Preview
@Composable
private fun SearchViewPreview() {
    ThemeProvider {
        SearchView(SearchState().apply { isVisible = true })
    }
}