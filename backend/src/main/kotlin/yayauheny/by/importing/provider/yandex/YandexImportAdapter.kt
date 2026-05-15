package yayauheny.by.importing.provider.yandex

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

class YandexImportAdapter : ImportSourceAdapter {
    override val provider: ImportProvider = ImportProvider.YANDEX_MAPS

    private val parser = YandexMapsScrapedParser()

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
                message = error.message ?: "Invalid Yandex payload",
                inboxMetadata = metadata,
                cause = error
            )
        }
    }

    override fun extractInboxMetadata(rawPayload: JsonObject): InboxMetadata = rawPayload.toInboxMetadata()

    override fun extractSourceLocation(rawPayload: JsonObject): SourceLocationHint =
        SourceLocationHint(
            country = rawPayload.stringValue("country"),
            city = rawPayload.canonicalCity(),
            lat = rawPayload.locationCoordinate("lat"),
            lng = rawPayload.locationCoordinate("lng")
        )

    private fun JsonObject.toInboxMetadata(): InboxMetadata =
        InboxMetadata(
            provider = provider,
            entityType = ImportEntityType.PLACE,
            externalId =
                this["placeId"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() },
            sourceUrl =
                listOf("url", "searchUrl", "yandexUri")
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

    private fun JsonObject.canonicalCity(): String? =
        stringValue("city")
            ?: stringValue("state")?.takeUnless(::looksLikeRegion)
            ?: extractCityFromAddress(stringValue("address"))

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

    private fun extractCityFromAddress(address: String?): String? =
        address
            ?.split(',')
            ?.mapNotNull { token -> normalizeLocalityToken(token) }
            ?.firstOrNull()

    private fun normalizeLocalityToken(token: String): String? {
        val trimmed = token.trim().replace(Regex("\\s+"), " ")
        if (trimmed.isEmpty()) {
            return null
        }

        val lower = trimmed.lowercase()
        if (looksLikeRegion(trimmed) || STREET_MARKERS.any(lower::contains)) {
            return null
        }

        val normalized = trimmed.replace(LOCALITY_PREFIX, "").trim()
        return normalized.takeIf { it.isNotEmpty() && !looksLikeRegion(it) }
    }

    private fun looksLikeRegion(value: String): Boolean {
        val normalized = value.lowercase()
        return REGION_MARKERS.any(normalized::contains)
    }

    private companion object {
        val LOCALITY_PREFIX =
            Regex("^(г\\.?|город|д\\.?|деревня|пос\\.?|поселок|посёлок|пгт|с\\.?|село|аг\\.?|агрогородок)\\s+", RegexOption.IGNORE_CASE)
        val REGION_MARKERS =
            listOf(
                "область",
                "обл.",
                "край",
                "республика",
                "район",
                "автономный округ",
                "province",
                "region"
            )
        val STREET_MARKERS =
            listOf(
                "ул.",
                "улица",
                "просп",
                "пр-т",
                "переулок",
                "шоссе",
                "бульвар",
                "тракт",
                "трасса"
            )
    }
}
