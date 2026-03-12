package yayauheny.by.service.import.twogis

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import yayauheny.by.model.import.twogis.TwoGisScrapedLocation
import yayauheny.by.model.import.twogis.TwoGisScrapedPlace
import yayauheny.by.service.import.Parser
import yayauheny.by.service.import.requireObject
import yayauheny.by.service.import.requireString
import yayauheny.by.service.import.InvalidImportPayload
import yayauheny.by.service.import.optionalArray
import yayauheny.by.service.import.optionalString

/**
 * Парсер для scraped формата 2ГИС.
 * Преобразует JsonObject в TwoGisScrapedPlace.
 */
class TwoGisScrapedParser : Parser<TwoGisScrapedPlace> {
    private val logger = LoggerFactory.getLogger(TwoGisScrapedParser::class.java)

    override fun parse(jsonObject: JsonObject): TwoGisScrapedPlace {
        return try {
            val id = jsonObject.requireString("id")
            val title =
                resolveTitle(
                    jsonObject.optionalString("title"),
                    jsonObject.optionalArray("rubrics"),
                    id
                )
            val category = jsonObject.optionalString("category")
            val address =
                resolveAddress(
                    jsonObject["address"],
                    jsonObject.optionalString("street"),
                    jsonObject.optionalString("houseNumber")
                )
            val locationObj = jsonObject.requireObject("location")
            val location = parseLocation(locationObj)

            val workingHours =
                when (val wh = jsonObject["working_hours"]) {
                    null, is JsonNull -> null
                    else -> wh.jsonObject
                }

            val attributeGroups =
                jsonObject.optionalArray("attributeGroups")?.mapNotNull { element ->
                    (element as? JsonPrimitive)?.content
                } ?: emptyList()

            val rubrics =
                jsonObject.optionalArray("rubrics")?.mapNotNull { element ->
                    (element as? JsonPrimitive)?.content
                } ?: emptyList()

            TwoGisScrapedPlace(
                id = id,
                title = title,
                category = category,
                address = address,
                location = location,
                workingHours = workingHours,
                attributeGroups = attributeGroups,
                rubrics = rubrics
            )
        } catch (e: InvalidImportPayload) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to parse TwoGisScrapedPlace from JSON", e)
            throw InvalidImportPayload("Failed to parse scraped place: ${e.message ?: e.javaClass.simpleName}")
        }
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

    private fun resolveTitle(
        title: String?,
        rubrics: kotlinx.serialization.json.JsonArray?,
        id: String
    ): String {
        if (!title.isNullOrBlank()) return title.trim()
        return "Туалет"
    }

    private fun resolveAddress(
        addressElem: kotlinx.serialization.json.JsonElement?,
        street: String?,
        houseNumber: String?
    ): String {
        val addr =
            when (addressElem) {
                null, is JsonNull -> null
                is JsonPrimitive -> addressElem.content.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
                else -> null
            }
        if (!addr.isNullOrBlank()) return addr.trim()
        val streetTrim = street?.trim()?.takeIf { !it.equals("null", ignoreCase = true) }
        val houseTrim = houseNumber?.trim()?.takeIf { !it.equals("null", ignoreCase = true) }
        return when {
            !streetTrim.isNullOrBlank() && !houseTrim.isNullOrBlank() -> "$streetTrim, $houseTrim"
            !streetTrim.isNullOrBlank() -> streetTrim
            !houseTrim.isNullOrBlank() -> houseTrim
            else -> ""
        }
    }
}
