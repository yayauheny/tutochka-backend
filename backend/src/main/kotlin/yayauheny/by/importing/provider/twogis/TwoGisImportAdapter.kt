package yayauheny.by.importing.provider.twogis

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component
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

@Component
class TwoGisImportAdapter : ImportSourceAdapter {
    override val provider: ImportProvider = ImportProvider.TWO_GIS

    private val parser = TwoGisScrapedParser()

    override fun parseEnvelope(
        rawPayload: JsonNode,
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

    override fun extractInboxMetadata(rawPayload: JsonNode): InboxMetadata = rawPayload.toInboxMetadata()

    override fun extractSourceLocation(rawPayload: JsonNode): SourceLocationHint =
        SourceLocationHint(
            country = rawPayload.stringValue("country"),
            city = rawPayload.stringValue("city"),
            lat = rawPayload.locationCoordinate("lat"),
            lng = rawPayload.locationCoordinate("lng")
        )

    private fun JsonNode.toInboxMetadata(): InboxMetadata =
        InboxMetadata(
            provider = provider,
            entityType = ImportEntityType.PLACE,
            externalId =
                this.get("id")
                    ?.takeIf { it.isTextual || it.isNumber }
                    ?.asText()
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() },
            sourceUrl =
                listOf("url", "searchUrl")
                    .firstNotNullOfOrNull { key ->
                        this.get(key)
                            ?.takeIf { it.isTextual }
                            ?.asText()
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                    },
            scrapedAt = PayloadHashing.parseInstantOrNull(this.get("scrapedAt")?.takeIf { it.isTextual }?.asText()),
            payloadHash = PayloadHashing.canonicalPayloadHash(this)
        )

    private fun JsonNode.stringValue(key: String): String? =
        this.get(key)
            ?.takeIf { it.isTextual }
            ?.asText()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun JsonNode.locationCoordinate(key: String): Double? =
        this.get("location")
            ?.takeIf { it.isObject }
            ?.get(key)
            ?.takeIf { it.isNumber || it.isTextual }
            ?.asDouble()
}
