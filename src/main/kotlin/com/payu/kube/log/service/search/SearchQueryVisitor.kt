package com.payu.kube.log.service.search

import SearchQueryBaseVisitor
import SearchQueryParser
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
        val text = ctx?.StringLiteral()?.text ?: return null
        return TextQuery(text.substring(1, text.lastIndex))
    }

    override fun visitRegex(ctx: SearchQueryParser.RegexContext?): Query? {
        val text = ctx?.RegexLiteral()?.text ?: return null
        val regex = runCatching { text.substring(2, text.lastIndex).toRegex() }.getOrNull() ?: return null
        return RegexQuery(regex)
    }
}