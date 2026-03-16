package com.vibely.pos.backend.common

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/**
 * Extension function to conditionally add a String field to a JSON object only if the value is non-null.
 *
 * @param key The JSON key
 * @param value The optional String value
 */
fun JsonObjectBuilder.putIfNotNull(key: String, value: String?) {
    value?.let { put(key, it) }
}

/**
 * Extension function to conditionally add a Double field to a JSON object only if the value is non-null.
 *
 * @param key The JSON key
 * @param value The optional Double value
 */
fun JsonObjectBuilder.putIfNotNull(key: String, value: Double?) {
    value?.let { put(key, it) }
}

/**
 * Extension function to conditionally add an Int field to a JSON object only if the value is non-null.
 *
 * @param key The JSON key
 * @param value The optional Int value
 */
fun JsonObjectBuilder.putIfNotNull(key: String, value: Int?) {
    value?.let { put(key, it) }
}

/**
 * Extension function to conditionally add a Boolean field to a JSON object only if the value is non-null.
 *
 * @param key The JSON key
 * @param value The optional Boolean value
 */
fun JsonObjectBuilder.putIfNotNull(key: String, value: Boolean?) {
    value?.let { put(key, it) }
}
