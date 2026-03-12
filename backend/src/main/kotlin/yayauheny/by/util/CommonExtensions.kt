package yayauheny.by.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.jooq.JSONB

private val jsonParser =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

fun JSONB?.toJsonObject(): JsonObject? = this?.data()?.let { runCatching { jsonParser.parseToJsonElement(it).jsonObject }.getOrNull() }

fun JsonObject?.toJSONB(): JSONB? = this?.let { JSONB.jsonb(it.toString()) }
