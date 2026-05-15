package yayauheny.by.importing.provider.twogis

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.Parser
import yayauheny.by.importing.provider.requireObject
import yayauheny.by.importing.provider.requireString
import yayauheny.by.model.import.NormalizedRestroomCandidate

class TwoGisScrapedParser : Parser<TwoGisScrapedPlace> {
    private val logger = LoggerFactory.getLogger(TwoGisScrapedParser::class.java)

    override fun parse(jsonObject: JsonObject): TwoGisScrapedPlace {
        return try {
            val id = jsonObject.requireString("id")
            val title = jsonObject.stringOrNull("title")
            val category = jsonObject.stringOrNull("category")
            val address = parseAddress(jsonObject["address"])
            val street = jsonObject.stringOrNull("street")
            val houseNumber = jsonObject.stringOrNull("houseNumber")
            val locationObj = jsonObject.requireObject("location")
            val location = parseLocation(locationObj)
            val workingHours =
                when (val wh = jsonObject["working_hours"]) {
                    null, is JsonNull -> null
                    else -> wh.jsonObject
                }
            val attributeGroups = jsonObject.stringList("attributeGroups")
            val rubrics = jsonObject.stringList("rubrics")

            TwoGisScrapedPlace(
                id = id,
                title = title,
                category = category,
                address = address,
                street = street,
                houseNumber = houseNumber,
                location = location,
                workingHours = workingHours,
                attributeGroups = attributeGroups,
                rubrics = rubrics
            )
        } catch (error: InvalidImportPayload) {
            throw error
        } catch (error: Exception) {
            logger.error("Failed to parse TwoGisScrapedPlace from JSON", error)
            throw InvalidImportPayload("Failed to parse scraped place: ${error.message ?: error.javaClass.simpleName}")
        }
    }

    override fun toCommonModel(
        providerDto: TwoGisScrapedPlace,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate {
        val resolved = TwoGisNormalizationProfile.resolve(providerDto)
        return NormalizedRestroomCandidate(
            provider =
                yayauheny.by.model.enums.ImportProvider
                    .fromPayloadType(context.payloadType),
            providerObjectId = providerDto.id,
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

    private fun parseLocation(locationObj: JsonObject): TwoGisScrapedLocation {
        val lat =
            locationObj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lat'")
        val lng =
            locationObj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lng'")
        return TwoGisScrapedLocation(lat = lat, lng = lng)
    }

    private fun parseAddress(addressElem: JsonElement?): String? =
        when (addressElem) {
            null, is JsonNull -> null
            is JsonPrimitive -> addressElem.content.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }?.trim()
            else -> null
        }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)
            ?.content
            ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }

    private fun JsonObject.stringList(key: String): List<String> =
        (this[key] as? JsonArray)
            ?.mapNotNull { element ->
                (element as? JsonPrimitive)
                    ?.content
                    ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            }.orEmpty()
}
