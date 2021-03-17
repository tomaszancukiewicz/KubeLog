package com.payu.kube.log.service.search

import SearchQueryLexer
import SearchQueryParser
import com.payu.kube.log.service.search.query.Query
import com.payu.kube.log.service.search.query.TextQuery
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.springframework.stereotype.Service

@Service
class SearchQueryCompilerService {

    fun compile(text: String) : Query {
        val inputStream = CharStreams.fromString(text)
        val lexer = SearchQueryLexer(inputStream)
        val commonTokenStream = CommonTokenStream(lexer)
        val parser = SearchQueryParser(commonTokenStream)
        val root = parser.fullQuery()
        val value = SearchQueryVisitor().visit(root)
        return value ?: TextQuery(text)
    }

}