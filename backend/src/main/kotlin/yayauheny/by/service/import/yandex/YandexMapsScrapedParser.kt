package yayauheny.by.service.import.yandex

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import yayauheny.by.model.import.yandex.YandexMapsScrapedLocation
import yayauheny.by.model.import.yandex.YandexMapsScrapedPlace
import yayauheny.by.service.import.InvalidImportPayload
import yayauheny.by.service.import.Parser

class YandexMapsScrapedParser : Parser<YandexMapsScrapedPlace> {
    override fun parse(jsonObject: JsonObject): YandexMapsScrapedPlace {
        val title =
            (jsonObject["title"] as? JsonPrimitive)
                ?.content
                ?.takeIf { it.isNotBlank() }
                ?: "Туалет"
        val shortTitle =
            (jsonObject["shortTitle"] as? JsonPrimitive)
                ?.content
                ?.takeIf { it.isNotBlank() }
        val categoryName =
            (jsonObject["categoryName"] as? JsonPrimitive)
                ?.content
                ?.takeIf { it.isNotBlank() }
        val address =
            when (val addr = jsonObject["address"]) {
                null, is JsonNull -> null
                is JsonPrimitive ->
                    addr.content
                        .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
                else -> null
            }

        val locationObj =
            jsonObject["location"]?.jsonObject
                ?: throw InvalidImportPayload("Missing 'location' object for Yandex place")
        val lat =
            locationObj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lat'")
        val lng =
            locationObj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lng'")

        val placeId =
            (jsonObject["placeId"] as? JsonPrimitive)
                ?.content
                ?.takeIf { it.isNotBlank() }
                ?: throw InvalidImportPayload("placeId is required for Yandex place")

        return YandexMapsScrapedPlace(
            title = title,
            shortTitle = shortTitle,
            categoryName = categoryName,
            categories = emptyList(),
            address = address,
            state = (jsonObject["state"] as? JsonPrimitive)?.content,
            country = (jsonObject["country"] as? JsonPrimitive)?.content,
            website = null,
            urls = emptyList(),
            phone = null,
            phoneUnformatted = null,
            location = YandexMapsScrapedLocation(lat = lat, lng = lng),
            totalScore = null,
            ratingCount = null,
            reviewsCount = null,
            reviewCrop = null,
            reviewText = null,
            statusText = (jsonObject["statusText"] as? JsonPrimitive)?.content,
            isOpenNow =
                (jsonObject["isOpenNow"] as? JsonPrimitive)
                    ?.content
                    ?.toBooleanStrictOrNull(),
            placeId = placeId,
            yandexUri = (jsonObject["yandexUri"] as? JsonPrimitive)?.content,
            geoId = (jsonObject["geoId"] as? JsonPrimitive)?.content,
            url = (jsonObject["url"] as? JsonPrimitive)?.content,
            searchString = (jsonObject["searchString"] as? JsonPrimitive)?.content,
            openingHoursText = (jsonObject["openingHoursText"] as? JsonPrimitive)?.content,
            workingHours = null,
            features = emptyList(),
            socialLinks = emptyList(),
            metro = emptyList(),
            metroDistanceM = emptyList(),
            stops = emptyList(),
            photos = emptyList(),
            photosCount = null,
            scrapedAt = (jsonObject["scrapedAt"] as? JsonPrimitive)?.content,
            extra = null
        )
    }
}
