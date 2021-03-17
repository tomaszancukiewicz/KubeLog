package com.payu.kube.log.ui.tab

import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.service.search.query.TextQuery
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority

class SearchBoxView : HBox() {

    companion object {
        private val EXECUTE_SEARCH_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ENTER)
        private val CLEAR_SEARCH_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ESCAPE)
    }

    data class Search(val query: Query, val type: SearchType)

    enum class SearchType {
        MARK, FILTER
    }

    private val searchTextField: TextField
    private val searchTypeChoiceBox: ChoiceBox<SearchType>

    val searchProperty: ObjectBinding<Search?>

    var queryFactory: ((String) -> Query) = { TextQuery(it) }
    var searchAction: ((Search?) -> Unit)? = null

    init {
        managedProperty().bind(visibleProperty())

        searchTextField = TextField()
        searchTextField.promptText = "Search in logs"
        setHgrow(searchTextField, Priority.ALWAYS)

        searchTypeChoiceBox = ChoiceBox<SearchType>()
        searchTypeChoiceBox.items.addAll(
            SearchType.MARK,
            SearchType.FILTER
        )
        searchTypeChoiceBox.value = searchTypeChoiceBox.items.firstOrNull()

        searchTextField.setOnKeyPressed {
            if (CLEAR_SEARCH_KEY_CODE_COMBINATION.match(it)) {
                searchTextField.text = ""
                it.consume()
            } else if (EXECUTE_SEARCH_KEY_CODE_COMBINATION.match(it)) {
                search()
                it.consume()
            }
        }

        searchProperty = Bindings.createObjectBinding({
            val searchType = searchTypeChoiceBox.selectionModel.selectedItemProperty().value
            searchTextField.textProperty().value
                .takeIf { it.isNotEmpty() && visibleProperty().value }
                ?.let { Search(queryFactory(it), searchType)}
        },
            searchTextField.textProperty(),
            visibleProperty(),
            searchTypeChoiceBox.selectionModel.selectedItemProperty()
        )

        searchProperty.addListener { _ ->
            search()
        }

        children.addAll(searchTextField, searchTypeChoiceBox)
    }

    private fun search() {
        searchAction?.invoke(searchProperty.value)
    }

    fun requestFocusSearchField() {
        if (isVisible) {
            searchTextField.requestFocus()
        }
    }
}