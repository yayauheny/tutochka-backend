package yayauheny.by.service.import.twogis

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.model.import.twogis.TwoGisScrapedPlace
import yayauheny.by.service.import.Normalizer

/**
 * Нормализует данные из scraped формата 2ГИС в каноническую модель NormalizedRestroomCandidate.
 */
class TwoGisScrapedNormalizer : Normalizer<TwoGisScrapedPlace> {
    override fun normalize(
        cityId: UUID,
        place: TwoGisScrapedPlace,
        payloadType: ImportPayloadType
    ): NormalizedRestroomCandidate {
        val attrs = place.attributeGroups.map { it.trim().lowercase() }.toSet()

        val toiletContext = determineToiletContext(place.category)
        val feeType = determineFeeType(attrs)
        val placeType = mapCategoryToPlaceType(place.category)
        val amenities = buildAmenities(attrs, toiletContext)
        val status = determineStatus(attrs)

        return NormalizedRestroomCandidate(
            provider = ImportProvider.fromPayloadType(payloadType),
            providerObjectId = place.id,
            cityId = cityId,
            name = place.title,
            address = place.address.takeIf { it.isNotBlank() },
            lat = place.location.lat,
            lng = place.location.lng,
            placeType = placeType,
            locationType = toiletContext,
            feeType = feeType,
            accessibilityType = AccessibilityType.UNKNOWN,
            status = status,
            amenities = amenities,
            rawSchedule = place.workingHours
        )
    }

    private fun determineToiletContext(category: String?): LocationType {
        return when (category?.lowercase()) {
            "toilet" -> LocationType.STANDALONE
            null -> LocationType.UNKNOWN
            else -> LocationType.INSIDE_BUILDING
        }
    }

    private fun determineFeeType(attrs: Set<String>): FeeType {
        // Проверяем "бесплатный" первым, так как "бесплатный туалет" содержит слово "платный"
        return when {
            attrs.any { it.contains("бесплатный туалет") || it == "бесплатный туалет" } -> FeeType.FREE
            attrs.any { it.contains("платный туалет") || it == "платный туалет" } -> FeeType.PAID
            else -> FeeType.UNKNOWN
        }
    }

    private fun mapCategoryToPlaceType(category: String?): PlaceType {
        return when (category?.lowercase()) {
            "toilet" -> PlaceType.PUBLIC
            "mall" -> PlaceType.MALL
            "mart", "market", "construction_hypermarket" -> PlaceType.MARKET
            "gas_station" -> PlaceType.GAS_STATION
            "food_service" -> PlaceType.FAST_FOOD
            "coffee_shop", "food_restaurant", "bar" -> PlaceType.RESTAURANT
            "bus_station", "railway_station" -> PlaceType.TRANSPORT
            "business_center" -> PlaceType.OFFICE
            "karaoke" -> PlaceType.CULTURE
            "hotel" -> PlaceType.OTHER
            else -> PlaceType.OTHER
        }
    }

    private fun buildAmenities(
        attrs: Set<String>,
        locationType: LocationType
    ): JsonObject {
        val paymentMethods =
            buildJsonArray {
                if (attrs.any { it.contains("оплата картой") }) add(JsonPrimitive("card"))
                if (attrs.any { it.contains("наличный расчёт") }) add(JsonPrimitive("cash"))
                if (attrs.any { it.contains("оплата по qr-коду") }) add(JsonPrimitive("qr"))
                if (attrs.any { it.contains("оплата через банк") }) add(JsonPrimitive("bank"))
                if (attrs.any { it.contains("оплата через приложение") }) add(JsonPrimitive("app"))
                if (attrs.any { it.contains("перевод с карты") }) add(JsonPrimitive("card_transfer"))
            }

        val accessibleEntrance =
            attrs.any { it.contains("доступный вход") } ||
                attrs.any { it.contains("пандус") } ||
                attrs.any { it.contains("подъёмник") } ||
                attrs.any { it.contains("широкий лифт") } ||
                attrs.any { it.contains("автоматическая дверь") } ||
                attrs.any { it.contains("нет двери") }

        return buildJsonObject {
            put("payment_methods", paymentMethods)
            put("accessible_entrance", JsonPrimitive(accessibleEntrance))
            put("accessible_toilet", JsonPrimitive(attrs.any { it.contains("туалет для маломобильных людей") }))
            put("ramp", JsonPrimitive(attrs.any { it.contains("пандус") }))
            put("lift", JsonPrimitive(attrs.any { it.contains("подъёмник") } || attrs.any { it.contains("широкий лифт") }))
            put(
                "automatic_door",
                JsonPrimitive(attrs.any { it.contains("автоматическая дверь") } || attrs.any { it.contains("нет двери") })
            )
            put("access_limited", JsonPrimitive(attrs.any { it.contains("доступ ограничен") }))
            put("toilet_context", JsonPrimitive(locationType.name))
        }
    }

    private fun determineStatus(attrs: Set<String>): RestroomStatus {
        return if (attrs.any { it.contains("доступ ограничен") }) {
            RestroomStatus.INACTIVE
        } else {
            RestroomStatus.ACTIVE
        }
    }
}
