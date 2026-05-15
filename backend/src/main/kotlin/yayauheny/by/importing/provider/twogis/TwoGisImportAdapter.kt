package yayauheny.by.importing.provider.twogis

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import yayauheny.by.importing.dedup.PayloadHashing
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.model.ImportEntityType
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.importing.model.SourceLocationHint
import yayauheny.by.importing.provider.ImportEnvelopeParsingException
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.model.enums.ImportProvider

class TwoGisImportAdapter : ImportSourceAdapter {
    override val provider: ImportProvider = ImportProvider.TWO_GIS

    private val parser = TwoGisScrapedParser()

    override fun parseEnvelope(
        rawPayload: JsonObject,
        context: ImportAdapterContext
    ): ProviderImportEnvelope {
        val metadata = extractInboxMetadata(rawPayload)

        return try {
            val command = parser.convert(rawPayload, context)
            ProviderImportEnvelope(
                inboxMetadata = metadata,
                command = command,
                rawPayload = rawPayload
            )
        } catch (error: InvalidImportPayload) {
            throw ImportEnvelopeParsingException(
                message = error.message ?: "Invalid 2GIS payload",
                inboxMetadata = metadata,
                cause = error
            )
        }
    }

    override fun extractInboxMetadata(rawPayload: JsonObject): InboxMetadata = rawPayload.toInboxMetadata()

    override fun extractSourceLocation(rawPayload: JsonObject): SourceLocationHint =
        SourceLocationHint(
            country = rawPayload.stringValue("country"),
            city = rawPayload.stringValue("city"),
            lat = rawPayload.locationCoordinate("lat"),
            lng = rawPayload.locationCoordinate("lng")
        )

    private fun JsonObject.toInboxMetadata(): InboxMetadata =
        InboxMetadata(
            provider = provider,
            entityType = ImportEntityType.PLACE,
            externalId =
                this["id"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() },
            sourceUrl =
                listOf("url", "searchUrl")
                    .firstNotNullOfOrNull { key ->
                        this[key]
                            ?.jsonPrimitive
                            ?.contentOrNull
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                    },
            scrapedAt = PayloadHashing.parseInstantOrNull(this["scrapedAt"]?.jsonPrimitive?.contentOrNull),
            payloadHash = PayloadHashing.canonicalPayloadHash(this)
        )

    private fun JsonObject.stringValue(key: String): String? =
        this[key]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun JsonObject.locationCoordinate(key: String): Double? =
        this["location"]
            ?.jsonObject
            ?.get(key)
            ?.jsonPrimitive
            ?.contentOrNull
            ?.toDoubleOrNull()
}
