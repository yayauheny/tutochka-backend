package yayauheny.by.importing.provider.osm

import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import com.fasterxml.jackson.databind.JsonNode
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.Parser
import yayauheny.by.model.import.NormalizedRestroomCandidate

class OsmScrapedParser : Parser<OsmScrapedToilet> {
    fun extractLocation(rawPayload: JsonNode): OsmScrapedLocation = parseLocation(rawPayload)

    override fun parse(jsonObject: JsonNode): OsmScrapedToilet {
        val nodeId = jsonObject.requireNodeId()
        val tags = jsonObject.requireTags()

        validateUsableToilet(tags)

        return OsmScrapedToilet(
            nodeId = nodeId,
            tags = tags,
            location = parseLocation(jsonObject),
            sourceUrl = resolveSourceUrl(jsonObject, nodeId),
            country = jsonObject.optionalString("country") ?: tags.cleanedValue("addr:country"),
            city = jsonObject.optionalString("city") ?: tags.cleanedValue("addr:city")
        )
    }

    override fun toCommonModel(
        providerDto: OsmScrapedToilet,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate {
        val resolved = OsmNormalizationProfile.resolve(providerDto)
        return NormalizedRestroomCandidate(
            provider =
                yayauheny.by.model.enums.ImportProvider
                    .fromPayloadType(context.payloadType),
            providerObjectId = providerDto.nodeId,
            cityId = context.cityId,
            name = resolved.name,
            address = resolved.address,
            lat = providerDto.location.lat,
            lng = providerDto.location.lng,
            placeType = resolved.placeType,
            locationType = resolved.locationType,
            feeType = resolved.feeType,
            accessibilityType = resolved.accessibilityType,
            status = resolved.status,
            amenities = resolved.amenities,
            rawSchedule = resolved.rawSchedule,
            buildingContext = resolved.buildingContext,
            genderType = resolved.genderType
        )
    }

    private fun validateUsableToilet(tags: Map<String, String>) {
        if (!matchesSelectedToilet(tags)) {
            throw InvalidImportPayload("OSM payload does not match toilet selection")
        }

        if (tags["abandoned"].equals("yes", ignoreCase = true)) {
            throw InvalidImportPayload("OSM toilet is abandoned")
        }

        val access = tags.cleanedValue("access")?.lowercase()
        if (access in UNSUPPORTED_ACCESS_VALUES) {
            throw InvalidImportPayload("OSM toilet access=$access is not usable for public import")
        }
    }

    private fun matchesSelectedToilet(tags: Map<String, String>): Boolean {
        if (tags["amenity"].equals("toilets", ignoreCase = true)) {
            return true
        }

        if (tags["building"].equals("toilets", ignoreCase = true)) {
            return true
        }

        if (tags.cleanedValue("name")?.lowercase(Locale.ROOT) in supportedNameValues) {
            return true
        }

        if (tags.cleanedValue("name:ru")?.lowercase(Locale.ROOT) in supportedRussianNameValues) {
            return true
        }

        if (tags.cleanedValue("name:be")?.lowercase(Locale.ROOT)?.contains("туалет") == true) {
            return true
        }

        if (tags.cleanedValue("description")?.lowercase(Locale.ROOT)?.contains("туалет") == true) {
            return true
        }

        return tags.cleanedValue("description:ru")?.lowercase(Locale.ROOT)?.contains("туалет") == true
    }

    private fun parseLocation(rawPayload: JsonNode): OsmScrapedLocation {
        parseExplicitLocation(rawPayload)?.let { return it }

        val geom = rawPayload.optionalString("geom") ?: rawPayload.optionalString("geometry")
        if (geom != null) {
            return parseGeom(geom)
        }

        throw InvalidImportPayload("Missing coordinates: expected 'location', 'lat'/'lng', or 'geom'")
    }

    private fun parseExplicitLocation(rawPayload: JsonNode): OsmScrapedLocation? {
        val locationObject = rawPayload.get("location")?.takeIf { it.isObject }
        val lat =
            locationObject
                ?.get("lat")
                ?.takeIf { it.isNumber || it.isTextual }
                ?.asDouble()
                ?: rawPayload.get("lat")?.takeIf { it.isNumber || it.isTextual }?.asDouble()
        val lng =
            locationObject
                ?.get("lng")
                ?.takeIf { it.isNumber || it.isTextual }
                ?.asDouble()
                ?: rawPayload.get("lng")?.takeIf { it.isNumber || it.isTextual }?.asDouble()

        return if (lat != null && lng != null) {
            OsmScrapedLocation(lat = lat, lng = lng)
        } else {
            null
        }
    }

    private fun parseGeom(geom: String): OsmScrapedLocation {
        val match = POINT_REGEX.find(geom.trim()) ?: throw InvalidImportPayload("Unsupported OSM geom format: $geom")
        val x = match.groupValues[1].toDoubleOrNull() ?: throw InvalidImportPayload("Invalid geom x coordinate")
        val y = match.groupValues[2].toDoubleOrNull() ?: throw InvalidImportPayload("Invalid geom y coordinate")

        return if (x in -180.0..180.0 && y in -90.0..90.0) {
            OsmScrapedLocation(lat = y, lng = x)
        } else {
            OsmScrapedLocation(
                lat = (2.0 * atan(exp(y / EARTH_RADIUS_METERS)) - PI / 2.0) * 180.0 / PI,
                lng = x / EARTH_RADIUS_METERS * 180.0 / PI
            )
        }
    }

    private fun resolveSourceUrl(
        rawPayload: JsonNode,
        nodeId: String
    ): String =
        rawPayload.optionalString("sourceUrl")
            ?: rawPayload.optionalString("url")
            ?: "https://www.openstreetmap.org/node/$nodeId"

    private fun JsonNode.requireNodeId(): String =
        listOf("node_id", "nodeId", "id")
            .firstNotNullOfOrNull { key -> this.get(key)?.takeIf { it.isTextual || it.isNumber }?.asText()?.trim() }
            ?.replace(",", "")
            ?.takeIf { it.isNotBlank() }
            ?: throw InvalidImportPayload("Missing or invalid 'node_id'")

    private fun JsonNode.requireTags(): Map<String, String> {
        return readTags() ?: throw InvalidImportPayload("Missing or invalid 'tags'")
    }

    private fun JsonNode.optionalString(key: String): String? =
        this.get(key)
            ?.takeIf { it.isTextual }
            ?.asText()
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

    private fun Map<String, String>.cleanedValue(key: String): String? =
        this[key]
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

    private companion object {
        val POINT_REGEX = Regex("""(?:SRID=\d+;)?POINT\s*\(\s*([-0-9.]+)\s+([-0-9.]+)\s*\)""", RegexOption.IGNORE_CASE)
        const val EARTH_RADIUS_METERS = 6378137.0
        val UNSUPPORTED_ACCESS_VALUES = setOf("no", "private", "permit")
        val supportedNameValues = setOf("туалет", "wc", "toilet", "restroom")
        val supportedRussianNameValues = setOf("туалет", "общественный туалет", "wc")
    }
}
