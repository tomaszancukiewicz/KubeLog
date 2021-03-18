package com.payu.kube.log.service.search

import SearchQueryLexer
import SearchQueryParser
import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.service.search.query.TextQuery
import org.antlr.v4.runtime.*
import org.springframework.stereotype.Service

@Service
class SearchQueryCompilerService {

    fun compile(text: String) : Query {
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
        query.errors.addAll(errorListener.errors)
        return query
    }

}