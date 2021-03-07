package com.payu.kube.log.controller

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
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

    data class Search(val text: String, val type: String)

    object SearchTypes {
        const val MARK = "Mark"
        const val FILTER = "Show only matching"
        const val NOT_FILTER = "Show only not matching"
    }

    private val searchTextField: TextField
    private val searchTypeChoiceBox: ChoiceBox<String>

    private val searchTextProperty: StringBinding
    val searchProperty: ObjectBinding<Search>

    var searchAction: ((Search) -> Unit)? = null

    init {
        managedProperty().bind(visibleProperty())

        searchTextField = TextField()
        searchTextField.promptText = "Search in logs"
        setHgrow(searchTextField, Priority.ALWAYS)

        searchTypeChoiceBox = ChoiceBox<String>()
        searchTypeChoiceBox.items.addAll(
            SearchTypes.MARK,
            SearchTypes.FILTER,
            SearchTypes.NOT_FILTER
        )
        searchTypeChoiceBox.value = searchTypeChoiceBox.items.firstOrNull()

        searchTextProperty = Bindings.`when`(visibleProperty())
            .then(searchTextField.textProperty())
            .otherwise("")

        searchTextProperty.addListener { _ ->
            search()
        }
        searchTypeChoiceBox.selectionModel.selectedItemProperty().addListener { _ ->
            search()
        }
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
            val searchedText = searchTextProperty.value
            val searchType = searchTypeChoiceBox.selectionModel.selectedItemProperty().value
            Search(searchedText, searchType)
        }, searchTextProperty, searchTypeChoiceBox.selectionModel.selectedItemProperty())

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