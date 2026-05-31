package kotlinx.serialization.modules

import kotlin.reflect.KClass

object SerializersModule {
    operator fun invoke(block: SerializersModuleBuilder.() -> Unit = {}): SerializersModule {
        SerializersModuleBuilder().block()
        return this
    }
}

class SerializersModuleBuilder {
    fun <T : Any> contextual(
        kClass: KClass<T>,
        serializer: Any
    ) {
        @Suppress("UNUSED_VARIABLE")
        val ignored = kClass to serializer
    }
}
