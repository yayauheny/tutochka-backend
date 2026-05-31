package kotlinx.serialization

import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlinx.serialization.modules.SerializersModule

interface SerialFormat {
    val serializersModule: SerializersModule
}

interface StringFormat : SerialFormat {
    fun <T> encodeToString(
        serializer: KSerializer<T>,
        value: T
    ): String

    fun <T> decodeFromString(
        serializer: KSerializer<T>,
        string: String
    ): T
}

interface DeserializationStrategy<T>

interface SerializationStrategy<T>

interface KSerializer<T> : SerializationStrategy<T>, DeserializationStrategy<T>

object SerializersKt {
    @JvmStatic
    fun <T> serializerOrNull(
        module: SerializersModule,
        type: KType
    ): KSerializer<T>? = null

    @JvmStatic
    fun <T> serializerOrNull(
        module: SerializersModule,
        type: Type
    ): KSerializer<T>? = null
}
