package com.payu.kube.log.service.search

import com.payu.kube.log.search.query.SearchQueryBaseVisitor
import com.payu.kube.log.search.query.SearchQueryParser
import com.payu.kube.log.service.search.query.*

class SearchQueryVisitor: SearchQueryBaseVisitor<Query?>() {

    override fun visitFullQuery(ctx: SearchQueryParser.FullQueryContext?): Query? {
        return ctx?.query()?.let { visit(it) }
    }

    override fun visitSquareBracket(ctx: SearchQueryParser.SquareBracketContext?): Query? {
        return ctx?.query()?.let { visit(it) }
    }

    override fun visitCurlyBracket(ctx: SearchQueryParser.CurlyBracketContext?): Query? {
        return ctx?.query()?.let { visit(it) }
    }

    override fun visitBinaryOperation(ctx: SearchQueryParser.BinaryOperationContext?): Query? {
        val r1 = ctx?.query(0)?.let { visit(it) } ?: return null
        val r2 = ctx.query(1)?.let { visit(it) } ?: return null
        return when(ctx.BinaryOperator()?.text) {
            "AND" -> AndQuery(r1, r2)
            "OR" -> OrQuery(r1, r2)
            else -> null
        }
    }

    override fun visitUnaryOperation(ctx: SearchQueryParser.UnaryOperationContext?): Query? {
        ctx?.NOT() ?: return null
        return ctx.query()?.let { visit(it) }?.let { NotQuery(it) }
    }

    override fun visitFunction(ctx: SearchQueryParser.FunctionContext?): Query? {
        val functionName = ctx?.FunctionName()?.text ?: return null
        return ctx.query()?.let { visit(it) }?.let { FunctionQuery(functionName, it) }
    }

    override fun visitString(ctx: SearchQueryParser.StringContext?): Query? {
        val node = ctx?.StringLiteral()?.text ?: return null
        val text = node.substring(1, node.lastIndex).let {
            if (node[0] == '\"')
                it.replace("\\\"", "\"")
            else
                it.replace("\\'", "'")
        }
        return TextQuery(text)
    }

    override fun visitRegex(ctx: SearchQueryParser.RegexContext?): Query? {
        val node = ctx?.RegexLiteral()?.text ?: return null
        val text = node.substring(2, node.lastIndex).let {
            if (node[1] == '\"')
                it.replace("\\\"", "\"")
            else
                it.replace("\\'", "'")
        }
        val regex = runCatching { text.toRegex() }.getOrNull() ?: return null
        return RegexQuery(regex)
    }
}