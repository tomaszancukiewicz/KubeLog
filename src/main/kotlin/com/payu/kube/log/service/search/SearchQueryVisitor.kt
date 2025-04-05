package com.payu.kube.log.service.search

import com.payu.kube.log.search.query.SearchQueryBaseVisitor
import com.payu.kube.log.search.query.SearchQueryParser
import com.payu.kube.log.service.search.query.*

class SearchQueryVisitor: SearchQueryBaseVisitor<Query?>() {

    override fun visitFullQuery(ctx: SearchQueryParser.FullQueryContext?): Query? {
        return ctx?.query()?.let { visit(it) }
    }

    override fun visitBracket(ctx: SearchQueryParser.BracketContext?): Query? {
        return ctx?.query()?.let { visit(it) }
    }

    override fun visitOrOperation(ctx: SearchQueryParser.OrOperationContext?): Query? {
        val r1 = ctx?.query(0)?.let { visit(it) } ?: return null
        val r2 = ctx.query(1)?.let { visit(it) } ?: return null
        return OrQuery(r1, r2)
    }

    override fun visitAndOperation(ctx: SearchQueryParser.AndOperationContext?): Query? {
        val r1 = ctx?.query(0)?.let { visit(it) } ?: return null
        val r2 = ctx.query(1)?.let { visit(it) } ?: return null
        return AndQuery(r1, r2)
    }

    override fun visitNotOperation(ctx: SearchQueryParser.NotOperationContext?): Query? {
        return ctx?.query()?.let { visit(it) }?.let { NotQuery(it) }
    }

    override fun visitIdentifier(ctx: SearchQueryParser.IdentifierContext?): Query? {
        val text = ctx?.IdentifierLiteral()?.text ?: return null
        return TextQuery(text, true)
    }

    override fun visitString(ctx: SearchQueryParser.StringContext?): Query? {
        val node = ctx?.StringLiteral()?.text ?: return null
        val text = node.substring(1, node.lastIndex).let {
            if (node[0] == '\"')
                it.replace("\\\"", "\"")
            else
                it.replace("\\'", "'")
        }
        return TextQuery(text, false)
    }

    override fun visitRegex(ctx: SearchQueryParser.RegexContext?): Query? {
        val flags = ctx?.RegexIndicator()?.text ?: ""
        val node = ctx?.StringLiteral()?.text ?: return null
        val text = node.substring(1, node.lastIndex).let {
            if (node[1] == '\"')
                it.replace("\\\"", "\"")
            else
                it.replace("\\'", "'")
        }

        val options = buildSet {
            if ('i' in flags) add(RegexOption.IGNORE_CASE)
            if ('c' in flags) add(RegexOption.IGNORE_CASE)
        }
        val regex = runCatching { text.toRegex(options) }.getOrNull() ?: return null
        return RegexQuery(regex)
    }
}