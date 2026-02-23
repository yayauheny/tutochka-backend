package yayauheny.by.service.import.twogis

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.TwoGisCategory
import yayauheny.by.model.enums.TwoGisRubric
import yayauheny.by.model.import.BuildingContext
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.model.import.twogis.TwoGisScrapedPlace
import yayauheny.by.service.import.Normalizer

/**
 * Нормализует данные из scraped формата 2ГИС в каноническую модель NormalizedRestroomCandidate.
 * Тип размещения (отдельный туалет или внутри здания) определяется по TwoGisCategory, при пустом category — по первому совпадению в TwoGisRubric.
 *
 * Неизвестные значения (category, rubrics, attributeGroups) не маппятся в enum и не сохраняются:
 * — если category/rubric не найден в маппинге — используется fallback (PlaceType.OTHER, LocationType.UNKNOWN), ошибка не выбрасывается;
 * — атрибуты из attributeGroups, для которых нет маппинга, не попадают в amenities и не вызывают ошибку.
 */
class TwoGisScrapedNormalizer : Normalizer<TwoGisScrapedPlace> {
    override fun normalize(
        cityId: UUID,
        place: TwoGisScrapedPlace,
        payloadType: ImportPayloadType
    ): NormalizedRestroomCandidate {
        val attrs = place.attributeGroups.map { it.trim().lowercase() }.toSet()

        val locationType = resolveLocationType(place.category, place.rubrics)
        val twoGisCategory = TwoGisCategory.fromValue(place.category)
        val placeType = twoGisCategory?.placeType ?: resolvePlaceTypeFromRubrics(place.rubrics) ?: PlaceType.OTHER
        val feeType = determineFeeType(attrs)
        val amenities = buildAmenities(attrs, locationType)
        val accessibilityType = determineAccessibilityType(attrs)
        val status = determineStatus(attrs)
        val genderType = TwoGisGenderFromTitleResolver.resolve(place.title)

        val buildingContext =
            if (locationType == LocationType.INSIDE_BUILDING) {
                BuildingContext(
                    name = place.title.takeIf { it.isNotBlank() },
                    address = place.address.ifBlank { place.title?.takeIf { it.isNotBlank() } ?: "" },
                    workTime = place.workingHours,
                    externalId = place.id
                )
            } else {
                null
            }

        return NormalizedRestroomCandidate(
            provider = ImportProvider.fromPayloadType(payloadType),
            providerObjectId = place.id,
            cityId = cityId,
            name = place.title.takeIf { it.isNotBlank() },
            address = place.address.takeIf { it.isNotBlank() },
            lat = place.location.lat,
            lng = place.location.lng,
            placeType = placeType,
            locationType = locationType,
            feeType = feeType,
            accessibilityType = accessibilityType,
            status = status,
            amenities = amenities,
            rawSchedule = place.workingHours,
            buildingContext = buildingContext,
            genderType = genderType
        )
    }

    private fun resolveLocationType(
        category: String?,
        rubrics: List<String>
    ): LocationType =
        TwoGisCategory.fromValue(category)?.locationType
            ?: TwoGisRubric.resolveLocationTypeFromRubrics(rubrics)
            ?: LocationType.UNKNOWN

    private fun resolvePlaceTypeFromRubrics(rubrics: List<String>): PlaceType? =
        rubrics.firstNotNullOfOrNull { TwoGisRubric.fromValue(it)?.placeType }

    private fun determineFeeType(attrs: Set<String>): FeeType {
        // Проверяем "бесплатный" первым, так как "бесплатный туалет" содержит слово "платный"
        return when {
            attrs.any { it.contains("бесплатный туалет") || it == "бесплатный туалет" } -> FeeType.FREE
            attrs.any { it.contains("платный туалет") || it == "платный туалет" } -> FeeType.PAID
            else -> FeeType.UNKNOWN
        }
    }

    private fun determineAccessibilityType(attrs: Set<String>): AccessibilityType {
        val hasWheelchairToilet = attrs.any { it.contains("туалет для маломобильных людей") }
        val hasAccessibleEntrance =
            attrs.any { it.contains("доступный вход") } ||
                attrs.any { it.contains("пандус") } ||
                attrs.any { it.contains("подъёмник") } ||
                attrs.any { it.contains("широкий лифт") } ||
                attrs.any { it.contains("автоматическая дверь") } ||
                attrs.any { it.contains("нет двери") } ||
                attrs.any { it.contains("доступно") }
        val hasAccessLimited = attrs.any { it.contains("доступ ограничен") }
        return when {
            hasWheelchairToilet || hasAccessibleEntrance -> AccessibilityType.WHEELCHAIR
            hasAccessLimited -> AccessibilityType.INACCESSIBLE
            else -> AccessibilityType.UNKNOWN
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
