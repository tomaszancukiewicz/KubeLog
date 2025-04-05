package com.payu.kube.log.ui.compose.tab.content.search

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.payu.kube.log.service.search.SearchQueryCompilerService

enum class SearchType {
    FILTER, MARK,
}

class SearchState {
    var isVisible by mutableStateOf(false)
    var text by mutableStateOf("")
    var searchType by mutableStateOf(SearchType.FILTER)

    private val compiledQuery by derivedStateOf {
        text.takeIf { it.isNotEmpty() && isVisible }
            ?.let { SearchQueryCompilerService.compile(it) }
    }

    val query = derivedStateOf {
        compiledQuery?.query
    }

    val queryErrors = derivedStateOf {
        compiledQuery?.errors
    }

    fun toggleVisible() {
        isVisible = !isVisible
    }
}