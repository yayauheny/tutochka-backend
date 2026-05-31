package kotlinx.serialization.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal val jsonMapper: ObjectMapper = jacksonObjectMapper()

sealed interface JsonElement {
    fun toJacksonNode(): JsonNode
}

data class JsonPrimitive(val value: Any?) : JsonElement {
    val content: String
        get() = value?.toString() ?: "null"

    val contentOrNull: String?
        get() = value?.toString()

    val intOrNull: Int?
        get() =
            when (value) {
                is Int -> value
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }

    val longOrNull: Long?
        get() =
            when (value) {
                is Long -> value
                is Number -> value.toLong()
                is String -> value.toLongOrNull()
                else -> null
            }

    val doubleOrNull: Double?
        get() =
            when (value) {
                is Double -> value
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }

    val booleanOrNull: Boolean?
        get() =
            when (value) {
                is Boolean -> value
                is String -> value.toBooleanStrictOrNull()
                else -> null
            }

    override fun toJacksonNode(): JsonNode =
        when (val current = value) {
            null -> JsonNodeFactory.instance.nullNode()
            is JsonElement -> current.toJacksonNode()
            is String -> JsonNodeFactory.instance.textNode(current)
            is Boolean -> JsonNodeFactory.instance.booleanNode(current)
            is Int -> JsonNodeFactory.instance.numberNode(current)
            is Long -> JsonNodeFactory.instance.numberNode(current)
            is Short -> JsonNodeFactory.instance.numberNode(current.toInt())
            is Float -> JsonNodeFactory.instance.numberNode(current)
            is Double -> JsonNodeFactory.instance.numberNode(current)
            is Number -> JsonNodeFactory.instance.numberNode(current.toDouble())
            is Map<*, *> -> JsonObject(current.toJsonElementMap()).toJacksonNode()
            is Iterable<*> -> JsonArray(current.toJsonElementList()).toJacksonNode()
            else -> JsonNodeFactory.instance.textNode(current.toString())
        }
}

object JsonNull : JsonElement {
    override fun toJacksonNode(): JsonNode = JsonNodeFactory.instance.nullNode()

    override fun toString(): String = "null"
}

data class JsonObject(
    val values: Map<String, JsonElement>
) : JsonElement {
    operator fun get(key: String): JsonElement? = values[key]

    fun containsKey(key: String): Boolean = values.containsKey(key)

    fun getValue(key: String): JsonElement = values.getValue(key)

    fun filterKeys(predicate: (String) -> Boolean): JsonObject = JsonObject(values.filterKeys(predicate))

    override fun toJacksonNode(): JsonNode {
        val node = JsonNodeFactory.instance.objectNode()
        values.forEach { (key, value) ->
            node.set<JsonNode>(key, value.toJacksonNode())
        }
        return node
    }

    override fun toString(): String = jsonMapper.writeValueAsString(toJacksonNode())
}

data class JsonArray(
    val values: List<JsonElement>
) : JsonElement, List<JsonElement> by values {
    override fun toJacksonNode(): JsonNode {
        val node = JsonNodeFactory.instance.arrayNode()
        values.forEach { value ->
            node.add(value.toJacksonNode())
        }
        return node
    }

    override fun toString(): String = jsonMapper.writeValueAsString(toJacksonNode())
}

class JsonObjectBuilder {
    internal val values = linkedMapOf<String, JsonElement>()

    fun put(
        key: String,
        value: Any?
    ) {
        values[key] = value.toJsonElement()
    }
}

class JsonArrayBuilder {
    internal val values = mutableListOf<JsonElement>()

    fun add(value: Any?) {
        values += value.toJsonElement()
    }
}

fun buildJsonObject(block: JsonObjectBuilder.() -> Unit): JsonObject {
    val builder = JsonObjectBuilder()
    builder.block()
    return JsonObject(builder.values)
}

fun buildJsonArray(block: JsonArrayBuilder.() -> Unit): JsonArray {
    val builder = JsonArrayBuilder()
    builder.block()
    return JsonArray(builder.values)
}

fun JsonElement.toJacksonNode(): JsonNode = when (this) {
    is JsonPrimitive -> toJacksonNode()
    is JsonObject -> toJacksonNode()
    is JsonArray -> toJacksonNode()
    JsonNull -> JsonNodeFactory.instance.nullNode()
}

val JsonElement.jsonObject: JsonObject
    get() = this as JsonObject

val JsonElement.jsonArray: JsonArray
    get() = this as JsonArray

val JsonElement.jsonPrimitive: JsonPrimitive
    get() = this as JsonPrimitive

fun JsonObject.toJsonElementMap(): Map<String, JsonElement> = values

fun Iterable<*>.toJsonElementList(): List<JsonElement> = map { it.toJsonElement() }

fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull
        is JsonElement -> this
        is String,
        is Number,
        is Boolean -> JsonPrimitive(this)
        is Map<*, *> -> JsonObject(this.toJsonElementMap())
        is Iterable<*> -> JsonArray(this.toJsonElementList())
        else -> JsonPrimitive(this)
    }

fun Map<*, *>.toJsonElementMap(): Map<String, JsonElement> =
    entries.associate { (key, value) ->
        key.toString() to value.toJsonElement()
    }

open class Json protected constructor() : StringFormat {
    var ignoreUnknownKeys: Boolean = false
    var prettyPrint: Boolean = false
    override var serializersModule: SerializersModule = SerializersModule

    override fun <T> encodeToString(
        serializer: KSerializer<T>,
        value: T
    ): String =
        when (value) {
            is JsonElement -> jsonMapper.writeValueAsString(value.toJacksonNode())
            else -> jsonMapper.writeValueAsString(value)
        }

    override fun <T> decodeFromString(
        serializer: KSerializer<T>,
        string: String
    ): T = @Suppress("UNCHECKED_CAST")
    (jsonMapper.readValue(string, Any::class.java) as T)

    companion object Default : Json() {
        operator fun invoke(block: Json.() -> Unit = {}): Json {
            val instance = Json()
            instance.apply(block)
            return instance
        }

        fun parseToJsonElement(value: String): JsonElement = fromJacksonNode(jsonMapper.readTree(value))

        inline fun <reified T> decodeFromString(value: String): T {
            val node = jsonMapper.readTree(value)
            val type = typeOf<T>()
            return decodeNode(node, type)
        }
    }
}

fun Json.parseToJsonElement(value: String): JsonElement = Json.Default.parseToJsonElement(value)

inline fun <reified T> Json.decodeFromString(value: String): T = Json.Default.decodeFromString(value)

@PublishedApi
internal fun fromJacksonNode(node: JsonNode): JsonElement =
    when {
        node.isNull -> JsonNull
        node.isObject -> {
            val map =
                linkedMapOf<String, JsonElement>().apply {
                    node.fields().forEachRemaining { (key, child) ->
                        put(key, fromJacksonNode(child))
                    }
                }
            JsonObject(map)
        }
        node.isArray -> {
            val list =
                buildList {
                    node.forEach { child ->
                        add(fromJacksonNode(child))
                    }
                }
            JsonArray(list)
        }
        node.isBoolean -> JsonPrimitive(node.booleanValue())
        node.isIntegralNumber -> JsonPrimitive(node.longValue())
        node.isFloatingPointNumber -> JsonPrimitive(node.doubleValue())
        node.isTextual -> JsonPrimitive(node.asText())
        else -> JsonPrimitive(node.asText())
    }

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T> decodeNode(
    node: JsonNode,
    type: KType
): T {
    if (type.classifier == List::class && type.arguments.size == 1 && type.arguments[0].type?.classifier == JsonObject::class) {
        require(node.isArray) { "Expected JSON array when decoding ${type}" }
        return node.map { child ->
            fromJacksonNode(child).jsonObject
        } as T
    }
    if (type.classifier == JsonObject::class) {
        return fromJacksonNode(node).jsonObject as T
    }
    if (type.classifier == JsonArray::class) {
        return fromJacksonNode(node).jsonArray as T
    }
    if (type.classifier == JsonElement::class) {
        return fromJacksonNode(node) as T
    }

    val targetClass = (type.classifier as? KClass<*>)?.java
        ?: error("Unsupported JSON target type: $type")
    return jsonMapper.treeToValue(node, targetClass) as T
}
