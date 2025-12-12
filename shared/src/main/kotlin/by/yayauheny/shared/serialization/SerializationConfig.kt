package by.yayauheny.shared.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.UUID

/**
 * Shared serialization configuration for both backend and bot modules
 */
object SerializationConfig {
    /**
     * SerializersModule with contextual serializers for UUID and Instant
     */
    val serializersModule: SerializersModule = SerializersModule {
        contextual(UUID::class, by.yayauheny.shared.serialization.UUIDSerializer)
        contextual(Instant::class, by.yayauheny.shared.serialization.InstantSerializer)
    }

    /**
     * Default Json instance with shared configuration
     */
    val defaultJson: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        serializersModule = this@SerializationConfig.serializersModule
    }
}

