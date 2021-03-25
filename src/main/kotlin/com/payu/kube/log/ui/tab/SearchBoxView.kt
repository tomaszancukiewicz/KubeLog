package com.payu.kube.log.ui.tab

import com.payu.kube.log.service.search.query.*
import com.payu.kube.log.util.BindingsUtils.mapToBoolean
import com.payu.kube.log.util.BindingsUtils.mapToString
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration

class SearchBoxView : HBox() {

    companion object {
        private val EXECUTE_SEARCH_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ENTER)
        private val CLEAR_SEARCH_KEY_CODE_COMBINATION = KeyCodeCombination(KeyCode.ESCAPE)
    }

    data class Search(val query: Query, val type: SearchType)

    enum class SearchType {
        MARK, FILTER
    }

    enum class AddToSearchType {
        AND, OR, NOT
    }

    private val searchTextField: TextField
    private val searchTypeChoiceBox: ChoiceBox<SearchType>
    private val modeCircle: Rectangle
    private val errorTooltip: Tooltip

    val searchProperty: ObjectBinding<Search?>

    var queryFactory: ((String) -> Query) = { TextQuery(it) }
    var searchAction: ((Search?) -> Unit)? = null

    init {
        managedProperty().bind(visibleProperty())

        searchTextField = TextField()
        searchTextField.promptText = "Search in logs e.g. \"Hello\" OR \"world\""
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

        searchProperty.addListener { _, _, _ ->
            search()
        }

        modeCircle = Rectangle()
        modeCircle.heightProperty().bind(searchTextField.heightProperty())
        modeCircle.widthProperty().bind(modeCircle.heightProperty().divide(2))
        modeCircle.fillProperty().bind(
            Bindings.`when`(searchProperty.mapToBoolean { it?.query?.errors?.isEmpty() ?: false })
                .then(Color.valueOf("#2979FF"))
                .otherwise(Color.valueOf("#2bc140"))
        )

        errorTooltip = Tooltip()
        errorTooltip.textProperty().bind(searchProperty.mapToString { search ->
            search?.query
                ?.errors
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString("\n", "Literal mode.\nNot interpreted because of errors:\n") {
                    " - $it"
                } ?: "Interpreted mode"
        })
        errorTooltip.showDelay = Duration.ZERO
        Tooltip.install(modeCircle, errorTooltip)

        children.addAll(searchTextField, modeCircle, searchTypeChoiceBox)
    }

    private fun search() {
        searchAction?.invoke(searchProperty.value)
    }

    fun requestFocusSearchField() {
        if (isVisible) {
            searchTextField.requestFocus()
        }
    }

    fun addToSearch(type: AddToSearchType, text: String) {
        val actualQuery = searchProperty.value?.query
        val textQuery = TextQuery(text)
        val newQuery = if (actualQuery != null) {
            when (type) {
                AddToSearchType.AND -> AndQuery(actualQuery, textQuery)
                AddToSearchType.OR -> OrQuery(actualQuery, textQuery)
                AddToSearchType.NOT -> AndQuery(actualQuery, NotQuery(textQuery))
            }
        } else {
            if (type == AddToSearchType.NOT) {
                NotQuery(textQuery)
            } else {
                textQuery
            }
        }
        searchTextField.text = newQuery.toQueryString()
    }
}