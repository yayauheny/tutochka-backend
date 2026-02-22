package yayauheny.by.service.import.twogis

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import yayauheny.by.model.import.twogis.TwoGisScrapedLocation
import yayauheny.by.model.import.twogis.TwoGisScrapedPlace
import yayauheny.by.service.import.Parser

/**
 * Парсер для scraped формата 2ГИС.
 * Преобразует JsonObject в TwoGisScrapedPlace.
 */
class TwoGisScrapedParser : Parser<TwoGisScrapedPlace> {
    private val logger = LoggerFactory.getLogger(TwoGisScrapedParser::class.java)
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    override fun parse(jsonObject: JsonObject): TwoGisScrapedPlace {
        return try {
            val id =
                jsonObject["id"]?.jsonPrimitive?.content
                    ?: throw IllegalArgumentException("Missing required field: id")

            val title =
                resolveTitle(
                    jsonObject["title"]?.jsonPrimitive?.content,
                    jsonObject["rubrics"]?.jsonArray,
                    id
                )

            val category = jsonObject["category"]?.jsonPrimitive?.content

            val address =
                resolveAddress(
                    jsonObject["address"],
                    jsonObject["street"]?.jsonPrimitive?.content,
                    jsonObject["houseNumber"]?.jsonPrimitive?.content
                )

            val location =
                parseLocation(jsonObject["location"]?.jsonObject)
                    ?: throw IllegalArgumentException("Missing required field: location")

            val workingHours =
                when (val wh = jsonObject["working_hours"]) {
                    null, is JsonNull -> null
                    else -> wh.jsonObject
                }

            val attributeGroups =
                jsonObject["attributeGroups"]?.jsonArray?.mapNotNull { element ->
                    (element as? JsonPrimitive)?.content
                } ?: emptyList()

            val rubrics =
                jsonObject["rubrics"]?.jsonArray?.mapNotNull { element ->
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
        } catch (e: Exception) {
            logger.error("Failed to parse TwoGisScrapedPlace from JSON", e)
            throw IllegalArgumentException("Failed to parse scraped place: ${e.message}", e)
        }
    }

    private fun parseLocation(locationObj: JsonObject?): TwoGisScrapedLocation? {
        if (locationObj == null) return null

        val lat = locationObj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
        val lng = locationObj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull()

        return if (lat != null && lng != null) {
            TwoGisScrapedLocation(lat = lat, lng = lng)
        } else {
            null
        }
    }

    private fun resolveTitle(
        title: String?,
        rubrics: kotlinx.serialization.json.JsonArray?,
        id: String
    ): String {
        if (!title.isNullOrBlank()) return title.trim()
        val firstRubric = rubrics?.firstOrNull()?.let { (it as? JsonPrimitive)?.content }?.trim()
        if (!firstRubric.isNullOrBlank()) return firstRubric
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
                is JsonPrimitive -> addressElem.content.takeIf { it.isNotBlank() }
                else -> null
            }
        if (!addr.isNullOrBlank()) return addr.trim()
        val streetTrim = street?.trim()
        val houseTrim = houseNumber?.trim()
        return when {
            !streetTrim.isNullOrBlank() && !houseTrim.isNullOrBlank() -> "$streetTrim, $houseTrim"
            !streetTrim.isNullOrBlank() -> streetTrim
            !houseTrim.isNullOrBlank() -> houseTrim
            else -> ""
        }
    }
}
