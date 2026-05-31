package kotlinx.serialization

open class SerializationException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
