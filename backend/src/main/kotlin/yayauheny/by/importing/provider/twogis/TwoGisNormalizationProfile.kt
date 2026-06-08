package yayauheny.by.importing.provider.twogis

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import yayauheny.by.model.imports.BuildingContext

internal object TwoGisNormalizationProfile : ProviderNormalizationProfile<TwoGisScrapedPlace> {
    private val mapper = jacksonObjectMapper()

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
    ): JsonNode {
        val node = mapper.createObjectNode()
        val paymentMethods = node.putArray("payment_methods")
        if (attrs.any { it.contains("оплата картой") }) paymentMethods.add("card")
        if (attrs.any { it.contains("наличный расчёт") }) paymentMethods.add("cash")
        if (attrs.any { it.contains("оплата по qr-коду") }) paymentMethods.add("qr")
        if (attrs.any { it.contains("оплата через банк") }) paymentMethods.add("bank")
        if (attrs.any { it.contains("оплата через приложение") }) paymentMethods.add("app")
        if (attrs.any { it.contains("перевод с карты") }) paymentMethods.add("card_transfer")

        val accessibleEntrance =
            attrs.any { it.contains("доступный вход") } ||
                attrs.any { it.contains("пандус") } ||
                attrs.any { it.contains("подъёмник") } ||
                attrs.any { it.contains("широкий лифт") } ||
                attrs.any { it.contains("автоматическая дверь") } ||
                attrs.any { it.contains("нет двери") } ||
                attrs.any { it.contains("доступно") }

        node.put("accessible_entrance", accessibleEntrance)
        node.put("accessible_toilet", attrs.any { it.contains("туалет для маломобильных людей") })
        node.put("ramp", attrs.any { it.contains("пандус") })
        node.put("lift", attrs.any { it.contains("подъёмник") } || attrs.any { it.contains("широкий лифт") })
        node.put("automatic_door", attrs.any { it.contains("автоматическая дверь") } || attrs.any { it.contains("нет двери") })
        node.put("access_limited", attrs.any { it.contains("доступ ограничен") })
        node.put("toilet_context", locationType.name)

        return node
    }

    private fun resolveRawSchedule(providerDto: TwoGisScrapedPlace): JsonNode? = providerDto.workingHours

    private fun resolveBuildingContext(
        providerDto: TwoGisScrapedPlace,
        name: String?,
        address: String?,
        locationType: LocationType,
        rawSchedule: JsonNode?
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
