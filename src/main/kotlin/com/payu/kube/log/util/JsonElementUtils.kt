package com.payu.kube.log.util

import kotlinx.serialization.json.*

object JsonElementUtils {
    val JsonElement.jsonObjectOrNull: JsonObject?
        get() = this as? JsonObject

    private val JsonElement.jsonPrimitiveOrNull: JsonPrimitive?
        get() = this as? JsonPrimitive

    val JsonElement.jsonArrayOrNull: JsonArray?
        get() = this as? JsonArray

    fun JsonElement.path(fieldName: String): JsonElement? {
        return this.jsonObjectOrNull?.get(fieldName)
    }

    fun JsonElement.asText(): String? {
        return this.jsonPrimitiveOrNull?.contentOrNull
    }

    fun JsonElement.asInt(): Int? {
        return this.jsonPrimitiveOrNull?.intOrNull
    }

    fun JsonElement.asBoolean(): Boolean? {
        return this.jsonPrimitiveOrNull?.booleanOrNull
    }
}