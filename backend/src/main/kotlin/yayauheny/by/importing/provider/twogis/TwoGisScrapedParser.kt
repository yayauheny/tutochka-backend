package yayauheny.by.importing.provider.twogis

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.Parser
import yayauheny.by.model.import.NormalizedRestroomCandidate

class TwoGisScrapedParser : Parser<TwoGisScrapedPlace> {
    private val logger = LoggerFactory.getLogger(TwoGisScrapedParser::class.java)

    override fun parse(jsonObject: JsonNode): TwoGisScrapedPlace {
        return try {
            val id = jsonObject.get("id")?.asText() ?: throw InvalidImportPayload("Missing id")
            val title = jsonObject.stringOrNull("title")
            val category = jsonObject.stringOrNull("category")
            val address = parseAddress(jsonObject.get("address"))
            val street = jsonObject.stringOrNull("street")
            val houseNumber = jsonObject.stringOrNull("houseNumber")
            val locationObj = jsonObject.get("location")?.takeIf { it.isObject } ?: throw InvalidImportPayload("Missing location")
            val location = parseLocation(locationObj)
            val workingHours = jsonObject.get("working_hours")?.takeIf { it.isObject }
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

    private fun parseLocation(locationObj: JsonNode): TwoGisScrapedLocation {
        val lat =
            locationObj.get("lat")?.takeIf { it.isNumber || it.isTextual }?.asDouble()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lat'")
        val lng =
            locationObj.get("lng")?.takeIf { it.isNumber || it.isTextual }?.asDouble()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lng'")
        return TwoGisScrapedLocation(lat = lat, lng = lng)
    }

    private fun parseAddress(addressElem: JsonNode?): String? =
        addressElem?.takeIf { it.isTextual }?.asText()?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }?.trim()

    private fun JsonNode.stringOrNull(key: String): String? =
        this.get(key)
            ?.takeIf { it.isTextual }
            ?.asText()
            ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }

    private fun JsonNode.stringList(key: String): List<String> =
        this.get(key)
            ?.takeIf { it.isArray }
            ?.mapNotNull { element ->
                element.takeIf { it.isTextual }
                    ?.asText()
                    ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            }.orEmpty()
}
