package yayauheny.by.importing.provider.osm

import com.fasterxml.jackson.databind.JsonNode
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

class OsmImportAdapter : ImportSourceAdapter {
    override val provider: ImportProvider = ImportProvider.OSM

    private val parser = OsmScrapedParser()

    override fun parseEnvelope(
        rawPayload: JsonNode,
        context: ImportAdapterContext
    ): ProviderImportEnvelope {
        val metadata = extractInboxMetadata(rawPayload)

        return try {
            ProviderImportEnvelope(
                inboxMetadata = metadata,
                command = parser.convert(rawPayload, context),
                rawPayload = rawPayload
            )
        } catch (error: InvalidImportPayload) {
            throw ImportEnvelopeParsingException(
                message = error.message ?: "Invalid OSM payload",
                inboxMetadata = metadata,
                cause = error
            )
        }
    }

    override fun extractInboxMetadata(rawPayload: JsonNode): InboxMetadata = rawPayload.toInboxMetadata()

    override fun extractSourceLocation(rawPayload: JsonNode): SourceLocationHint {
        val location = runCatching { parser.extractLocation(rawPayload) }.getOrNull()
        return SourceLocationHint(
            country = rawPayload.optionalString("country") ?: rawPayload.optionalTag("addr:country"),
            city = rawPayload.optionalString("city") ?: rawPayload.optionalTag("addr:city"),
            lat = location?.lat,
            lng = location?.lng
        )
    }

    private fun JsonNode.toInboxMetadata(): InboxMetadata =
        InboxMetadata(
            provider = provider,
            entityType = ImportEntityType.PLACE,
            externalId = externalNodeId(),
            sourceUrl = optionalString("sourceUrl") ?: optionalString("url") ?: osmNodeUrl(),
            scrapedAt = PayloadHashing.parseInstantOrNull(this.get("scrapedAt")?.takeIf { it.isTextual }?.asText()),
            payloadHash = PayloadHashing.canonicalPayloadHash(this)
        )

    private fun JsonNode.externalNodeId(): String? =
        listOf("node_id", "nodeId", "id")
            .firstNotNullOfOrNull { key ->
                this.get(key)
                    ?.takeIf { it.isTextual || it.isNumber }
                    ?.asText()
                    ?.replace(",", "")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }

    private fun JsonNode.osmNodeUrl(): String? = externalNodeId()?.let { "https://www.openstreetmap.org/node/$it" }

    private fun JsonNode.optionalString(key: String): String? =
        this.get(key)
            ?.takeIf { it.isTextual }
            ?.asText()
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

    private fun JsonNode.optionalTag(key: String): String? =
        readTags()
            ?.get(key)
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
}
