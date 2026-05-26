package yayauheny.by.importing.provider.osm

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
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

class OsmImportAdapter : ImportSourceAdapter {
    override val provider: ImportProvider = ImportProvider.OSM

    private val parser = OsmScrapedParser()

    override fun parseEnvelope(
        rawPayload: JsonObject,
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

    override fun extractInboxMetadata(rawPayload: JsonObject): InboxMetadata = rawPayload.toInboxMetadata()

    override fun extractSourceLocation(rawPayload: JsonObject): SourceLocationHint {
        val location = runCatching { parser.extractLocation(rawPayload) }.getOrNull()
        return SourceLocationHint(
            country = rawPayload.optionalString("country") ?: rawPayload.optionalTag("addr:country"),
            city = rawPayload.optionalString("city") ?: rawPayload.optionalTag("addr:city"),
            lat = location?.lat,
            lng = location?.lng
        )
    }

    private fun JsonObject.toInboxMetadata(): InboxMetadata =
        InboxMetadata(
            provider = provider,
            entityType = ImportEntityType.PLACE,
            externalId = externalNodeId(),
            sourceUrl = optionalString("sourceUrl") ?: optionalString("url") ?: osmNodeUrl(),
            scrapedAt = PayloadHashing.parseInstantOrNull(this["scrapedAt"]?.jsonPrimitive?.contentOrNull),
            payloadHash = PayloadHashing.canonicalPayloadHash(this)
        )

    private fun JsonObject.externalNodeId(): String? =
        listOf("node_id", "nodeId", "id")
            .firstNotNullOfOrNull { key ->
                this[key]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.replace(",", "")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            }

    private fun JsonObject.osmNodeUrl(): String? = externalNodeId()?.let { "https://www.openstreetmap.org/node/$it" }

    private fun JsonObject.optionalString(key: String): String? =
        this[key]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

    private fun JsonObject.optionalTag(key: String): String? =
        readTags()
            ?.get(key)
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
}
