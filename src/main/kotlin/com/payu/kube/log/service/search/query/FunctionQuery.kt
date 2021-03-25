package com.payu.kube.log.service.search.query

class FunctionQuery(val functionName: String, q: Query) : UnaryOperationQuery(q) {
    override fun toString(): String {
        return "FunctionQuery($functionName, $q, errors=$errors)"
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + functionName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (!super.equals(other))
            return false
        if (other !is FunctionQuery)
            return false
        return functionName == other.functionName
    }

    override fun check(text: String): Boolean {
        return super.check(useFunction(text))
    }

    override fun phrasesToMark(text: String): List<IntRange> {
        return super.phrasesToMark(useFunction(text))
    }

    private fun useFunction(text: String): String {
        return when(functionName) {
            "upperCase" -> text.toUpperCase()
            "lowerCase" -> text.toLowerCase()
            else -> text
        }
    }

    override fun toQueryString(): String {
        if (functionName == "upperCase" || functionName == "lowerCase") {
            return "$functionName(${q.toQueryString()})"
        }
        return q.toQueryString()
    }
}