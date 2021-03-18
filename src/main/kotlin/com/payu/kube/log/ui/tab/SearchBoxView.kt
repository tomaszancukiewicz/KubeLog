package com.payu.kube.log.ui.tab

import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.service.search.query.TextQuery
import com.payu.kube.log.util.BindingsUtils.mapToString
import com.payu.kube.log.util.ViewUtils.bindManagedAndVisibility
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

class SearchBoxView : VBox() {

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
    private val errorLabel: Label

    val searchProperty: ObjectBinding<Search?>

    var queryFactory: ((String) -> Query) = { TextQuery(it) }
    var searchAction: ((Search?) -> Unit)? = null

    init {
        managedProperty().bind(visibleProperty())

        searchTextField = TextField()
        searchTextField.promptText = "Search in logs e.g. \"Hello\" OR \"world\""
        HBox.setHgrow(searchTextField, Priority.ALWAYS)

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

        val hBox = HBox()
        hBox.children.addAll(searchTextField, searchTypeChoiceBox)

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

        errorLabel = Label()
        errorLabel.padding = Insets(5.0)
        errorLabel.textFill = Color.RED
        errorLabel.isWrapText = true
        errorLabel.textProperty().bind(searchProperty.mapToString { search ->
            search?.query
                ?.errors
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString("\n", "Errors in query found. It will be interpreted literally.\n") ?: ""
        })
        errorLabel.bindManagedAndVisibility(errorLabel.textProperty().isNotEmpty)

        children.addAll(hBox, errorLabel)
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