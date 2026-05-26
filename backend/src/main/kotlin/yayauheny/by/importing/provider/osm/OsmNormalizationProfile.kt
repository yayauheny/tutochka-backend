package yayauheny.by.importing.provider.osm

import java.security.MessageDigest
import java.util.Locale
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.importing.provider.ProviderNormalizationProfile
import yayauheny.by.importing.provider.ProviderResolvedFields
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.import.BuildingContext

internal object OsmNormalizationProfile : ProviderNormalizationProfile<OsmScrapedToilet> {
    override fun resolve(providerDto: OsmScrapedToilet): ProviderResolvedFields {
        val tags = providerDto.tags
        val name = resolveName(tags)
        val address = resolveAddress(tags)
        val hasInsideBuildingSignals = hasInsideBuildingSignals(tags)
        val buildingContext =
            if (hasInsideBuildingSignals) {
                resolveBuildingContext(providerDto, address, name)
            } else {
                null
            }
        val locationType = resolveLocationType(tags, buildingContext)
        val rawSchedule = resolveRawSchedule(tags)

        return ProviderResolvedFields(
            name = name,
            address = address,
            locationType = locationType,
            placeType = resolvePlaceType(tags, locationType),
            feeType = resolveFeeType(tags),
            accessibilityType = resolveAccessibilityType(tags),
            status = resolveStatus(tags),
            amenities = resolveAmenities(tags, locationType),
            rawSchedule = rawSchedule,
            buildingContext = buildingContext?.copy(workTime = rawSchedule),
            genderType = resolveGenderType(tags)
        )
    }

    private fun resolveName(tags: Map<String, String>): String =
        listOf("name", "name:ru", "name:be")
            .firstNotNullOfOrNull { key -> tags.cleanedValue(key) }
            ?: "Туалет"

    private fun resolveAddress(tags: Map<String, String>): String? {
        tags.cleanedValue("addr:full")?.let { return it }

        val street = tags.cleanedValue("addr:street")
        val houseNumber = tags.cleanedValue("addr:housenumber")

        return when {
            street != null && houseNumber != null -> "$street, $houseNumber"
            street != null -> street
            houseNumber != null -> houseNumber
            else -> null
        }
    }

    private fun resolveLocationType(
        tags: Map<String, String>,
        buildingContext: BuildingContext?
    ): LocationType =
        when {
            isStandaloneToilet(tags) -> LocationType.STANDALONE
            hasInsideBuildingSignals(tags) && buildingContext != null -> LocationType.INSIDE_BUILDING
            else -> LocationType.UNKNOWN
        }

    private fun resolvePlaceType(
        tags: Map<String, String>,
        locationType: LocationType
    ): PlaceType =
        when {
            tags.cleanedValue("tourism")?.equals("camp_site", ignoreCase = true) == true -> PlaceType.PARK
            locationType == LocationType.STANDALONE -> PlaceType.PUBLIC
            else -> PlaceType.OTHER
        }

    private fun resolveFeeType(tags: Map<String, String>): FeeType {
        val feeValue = tags.cleanedValue("fee")?.lowercase(Locale.ROOT)
        val chargeValue = tags.cleanedValue("charge")
        return when {
            feeValue == "no" -> FeeType.FREE
            feeValue != null -> FeeType.PAID
            !chargeValue.isNullOrBlank() -> FeeType.PAID
            tags.cleanedValue("toll")?.equals("yes", ignoreCase = true) == true -> FeeType.PAID
            else -> FeeType.UNKNOWN
        }
    }

    private fun resolveAccessibilityType(tags: Map<String, String>): AccessibilityType =
        when (tags.cleanedValue("wheelchair")?.lowercase(Locale.ROOT)) {
            "yes", "limited" -> AccessibilityType.WHEELCHAIR
            "no" -> AccessibilityType.INACCESSIBLE
            else -> AccessibilityType.UNKNOWN
        }

    private fun resolveStatus(tags: Map<String, String>): RestroomStatus =
        if (tags.cleanedValue("abandoned")?.equals("yes", ignoreCase = true) == true) {
            RestroomStatus.INACTIVE
        } else {
            RestroomStatus.ACTIVE
        }

    private fun resolveAmenities(
        tags: Map<String, String>,
        locationType: LocationType
    ): JsonObject =
        buildJsonObject {
            putOptional(tags, "access", "access")
            putOptional(tags, "operator", "operator")
            putOptional(tags, "charge", "charge")
            putOptional(tags, "toilets:disposal", "disposal")
            putOptional(tags, "toilets:position", "position")
            putOptional(tags, "level", "level")
            putOptional(tags, "addr:floor", "floor")
            putOptional(tags, "wheelchair", "wheelchair")
            put("changing_table", JsonPrimitive(tags.isTruthyOrLimited("changing_table")))
            put("drinking_water", JsonPrimitive(tags.isTruthy("drinking_water")))
            put("handwashing", JsonPrimitive(tags.isTruthy("toilets:handwashing")))
            put("portable", JsonPrimitive(tags.isTruthy("portable")))
            put("payment_required", JsonPrimitive(resolveFeeType(tags) == FeeType.PAID))
            put("accessible_toilet", JsonPrimitive(tags.isTruthyOrLimited("wheelchair")))
            put("access_limited", JsonPrimitive(isAccessLimited(tags.cleanedValue("access"))))
            put("toilet_context", JsonPrimitive(locationType.name))
        }

    private fun resolveRawSchedule(tags: Map<String, String>): JsonObject? =
        tags.cleanedValue("opening_hours")?.let { openingHours ->
            buildJsonObject {
                put("opening_hours", JsonPrimitive(openingHours))
                tags.cleanedValue("check_date:opening_hours")?.let { put("check_date", JsonPrimitive(it)) }
            }
        }

    private fun resolveBuildingContext(
        providerDto: OsmScrapedToilet,
        address: String?,
        restroomName: String
    ): BuildingContext? {
        if (address == null) {
            return null
        }

        val buildingName =
            listOf("operator", "name", "name:ru", "name:be")
                .firstNotNullOfOrNull { key -> providerDto.tags.cleanedValue(key) }
                ?.takeUnless { looksGenericRestroomName(it) || it.equals(restroomName, ignoreCase = true) }

        return BuildingContext(
            name = buildingName,
            address = address,
            workTime = null,
            externalId = synthesizeBuildingExternalId(providerDto, address, buildingName)
        )
    }

    private fun resolveGenderType(tags: Map<String, String>): GenderType {
        val hasMale = tags.isTruthy("male")
        val hasFemale = tags.isTruthy("female")
        val hasUnisex = tags.isTruthy("unisex")

        return when {
            hasUnisex || (hasMale && hasFemale) -> GenderType.UNISEX
            hasMale -> GenderType.MEN
            hasFemale -> GenderType.WOMEN
            else -> GenderType.UNKNOWN
        }
    }

    private fun hasInsideBuildingSignals(tags: Map<String, String>): Boolean {
        val indoor = tags.cleanedValue("indoor")?.lowercase(Locale.ROOT)
        if (indoor == "yes" || indoor == "room") {
            return true
        }

        if (tags.cleanedValue("location")?.equals("underground", ignoreCase = true) == true) {
            return true
        }

        if (!tags.cleanedValue("addr:floor").isNullOrBlank()) {
            return true
        }

        if (!tags.cleanedValue("level").isNullOrBlank()) {
            return true
        }

        val position = tags.cleanedValue("toilets:position")?.lowercase(Locale.ROOT).orEmpty()
        if (position.contains("inside")) {
            return true
        }

        val description = tags.cleanedValue("description")?.lowercase(Locale.ROOT).orEmpty()
        return description.contains("floor") || description.contains("этаж")
    }

    private fun isStandaloneToilet(tags: Map<String, String>): Boolean {
        val disposal = tags.cleanedValue("toilets:disposal")?.lowercase(Locale.ROOT)
        if (disposal in standaloneDisposalTypes) {
            return true
        }

        if (tags.isTruthy("portable")) {
            return true
        }

        if (tags.cleanedValue("toilets:type")?.equals("mobile", ignoreCase = true) == true) {
            return true
        }

        if (tags.cleanedValue("building")?.equals("toilets", ignoreCase = true) == true) {
            return true
        }

        return tags.cleanedValue("tourism")?.equals("camp_site", ignoreCase = true) == true
    }

    private fun isAccessLimited(access: String?): Boolean =
        when (access?.lowercase(Locale.ROOT)) {
            "customers", "permit", "private", "no" -> true
            else -> false
        }

    private fun synthesizeBuildingExternalId(
        providerDto: OsmScrapedToilet,
        address: String,
        buildingName: String?
    ): String {
        val seed =
            listOfNotNull(
                providerDto.country.cleanedValue(),
                providerDto.city.cleanedValue(),
                providerDto.tags.cleanedValue("addr:postcode"),
                address.cleanedValue(),
                buildingName.cleanedValue()
            ).joinToString("|")

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(seed.toByteArray())
                .joinToString("") { "%02x".format(it) }

        return "osm-building-$digest"
    }

    private fun looksGenericRestroomName(value: String): Boolean {
        val normalized = value.cleanedValue()?.lowercase(Locale.ROOT) ?: return true
        return normalized in genericRestroomNames
    }

    private fun JsonObjectBuilder.putOptional(
        tags: Map<String, String>,
        sourceKey: String,
        targetKey: String
    ) {
        tags.cleanedValue(sourceKey)?.let { put(targetKey, JsonPrimitive(it)) }
    }

    private fun Map<String, String>.cleanedValue(key: String): String? = this[key].cleanedValue()

    private fun Map<String, String>.isTruthy(key: String): Boolean = this[key].cleanedValue()?.equals("yes", ignoreCase = true) == true

    private fun Map<String, String>.isTruthyOrLimited(key: String): Boolean {
        val value = this[key].cleanedValue()?.lowercase(Locale.ROOT) ?: return false
        return value == "yes" || value == "limited"
    }

    private fun String?.cleanedValue(): String? =
        this
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

    private val standaloneDisposalTypes = setOf("pitlatrine", "bucket", "chemical", "septic_tank")
    private val genericRestroomNames = setOf("туалет", "wc", "ж", "м", "общественный туалет", "грамадскі туалет")
}
