package com.payu.kube.log.service.search

import com.payu.kube.log.search.query.SearchQueryLexer
import com.payu.kube.log.search.query.SearchQueryParser
import com.payu.kube.log.service.search.query.TextQuery
import org.antlr.v4.runtime.*

object SearchQueryCompilerService {

    fun compile(text: String): CompileQueryResult {
        val errorListener = ParseErrorListener()

        val inputStream = CharStreams.fromString(text)
        val lexer = SearchQueryLexer(inputStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)

        val commonTokenStream = CommonTokenStream(lexer)
        val parser = SearchQueryParser(commonTokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)

        val root = parser.fullQuery()
        val value = SearchQueryVisitor().visit(root)

        val query = value?.takeIf { errorListener.errors.isEmpty() } ?: TextQuery(text)

        return CompileQueryResult(
            query,
            errorListener.errors
        )
    }

}