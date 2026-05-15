package yayauheny.by.importing.provider.twogis

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.importing.provider.ProviderNormalizationProfile
import yayauheny.by.importing.provider.ProviderResolvedFields
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.TwoGisCategory
import yayauheny.by.model.enums.TwoGisRubric
import yayauheny.by.model.import.BuildingContext

internal object TwoGisNormalizationProfile : ProviderNormalizationProfile<TwoGisScrapedPlace> {
    override fun resolve(providerDto: TwoGisScrapedPlace): ProviderResolvedFields {
        val attrs = providerDto.attributeGroups.map { it.trim().lowercase() }.toSet()
        val name = resolveName(providerDto)
        val address = resolveAddress(providerDto)
        val locationType = resolveLocationType(providerDto)
        val placeType = resolvePlaceType(providerDto)
        val rawSchedule = resolveRawSchedule(providerDto)

        return ProviderResolvedFields(
            name = name,
            address = address,
            locationType = locationType,
            placeType = placeType,
            feeType = resolveFeeType(attrs),
            accessibilityType = resolveAccessibilityType(attrs),
            status = resolveStatus(attrs),
            amenities = resolveAmenities(attrs, locationType),
            rawSchedule = rawSchedule,
            buildingContext = resolveBuildingContext(providerDto, name, address, locationType, rawSchedule),
            genderType = resolveGenderType(providerDto)
        )
    }

    private fun resolveName(providerDto: TwoGisScrapedPlace): String =
        providerDto.title
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Туалет"

    private fun resolveAddress(providerDto: TwoGisScrapedPlace): String? {
        providerDto.address
            ?.trim()
            ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            ?.let { return it }

        val street = providerDto.street?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        val houseNumber = providerDto.houseNumber?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }

        return when {
            !street.isNullOrBlank() && !houseNumber.isNullOrBlank() -> "$street, $houseNumber"
            !street.isNullOrBlank() -> street
            !houseNumber.isNullOrBlank() -> houseNumber
            else -> null
        }
    }

    private fun resolveLocationType(providerDto: TwoGisScrapedPlace): LocationType =
        TwoGisCategory.fromValue(providerDto.category)?.locationType
            ?: TwoGisRubric.resolveLocationTypeFromRubrics(providerDto.rubrics)
            ?: LocationType.UNKNOWN

    private fun resolvePlaceType(providerDto: TwoGisScrapedPlace): PlaceType =
        TwoGisCategory.fromValue(providerDto.category)?.placeType
            ?: providerDto.rubrics.firstNotNullOfOrNull { TwoGisRubric.fromValue(it)?.placeType }
            ?: PlaceType.OTHER

    private fun resolveFeeType(attrs: Set<String>): FeeType =
        when {
            attrs.any { it.contains("бесплатный туалет") || it == "бесплатный туалет" } -> FeeType.FREE
            attrs.any { it.contains("платный туалет") || it == "платный туалет" } -> FeeType.PAID
            else -> FeeType.UNKNOWN
        }

    private fun resolveAccessibilityType(attrs: Set<String>): AccessibilityType {
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

    private fun resolveStatus(attrs: Set<String>): RestroomStatus =
        if (attrs.any { it.contains("доступ ограничен") }) {
            RestroomStatus.INACTIVE
        } else {
            RestroomStatus.ACTIVE
        }

    private fun resolveAmenities(
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

    private fun resolveRawSchedule(providerDto: TwoGisScrapedPlace): JsonObject? = providerDto.workingHours

    private fun resolveBuildingContext(
        providerDto: TwoGisScrapedPlace,
        name: String?,
        address: String?,
        locationType: LocationType,
        rawSchedule: JsonObject?
    ): BuildingContext? {
        if (locationType != LocationType.INSIDE_BUILDING) {
            return null
        }

        return BuildingContext(
            name = name?.takeIf { it.isNotBlank() },
            address = address ?: name.orEmpty(),
            workTime = rawSchedule,
            externalId = providerDto.id
        )
    }

    private fun resolveGenderType(providerDto: TwoGisScrapedPlace): GenderType {
        val title =
            providerDto.title
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return GenderType.UNKNOWN

        val lower = title.lowercase()
        val hasMale = lower.contains("мужской")
        val hasFemale = lower.contains("женский")

        return when {
            hasMale && hasFemale -> GenderType.UNISEX
            hasMale -> GenderType.MEN
            hasFemale -> GenderType.WOMEN
            else -> GenderType.UNKNOWN
        }
    }
}
