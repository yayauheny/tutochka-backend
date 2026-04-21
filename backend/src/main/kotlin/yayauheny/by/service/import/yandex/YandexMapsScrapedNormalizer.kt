package yayauheny.by.service.import.yandex

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
import yayauheny.by.model.import.BuildingContext
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.model.import.yandex.YandexMapsScrapedPlace
import yayauheny.by.service.import.InvalidImportPayload
import yayauheny.by.service.import.Normalizer

class YandexMapsScrapedNormalizer : Normalizer<YandexMapsScrapedPlace> {
    override fun normalize(
        cityId: UUID,
        place: YandexMapsScrapedPlace,
        payloadType: ImportPayloadType
    ): NormalizedRestroomCandidate {
        val provider = ImportProvider.fromPayloadType(payloadType)
        val providerObjectId =
            place.placeId.takeIf { it.isNotBlank() }
                ?: throw InvalidImportPayload("placeId is required for provider=$provider")

        val trimmedTitle = place.title.trim()
        val name =
            (
                place.shortTitle?.trim().takeUnless { it.isNullOrBlank() }
                    ?: trimmedTitle.takeUnless { it.isBlank() }
            )

        val address = place.address?.trim().takeUnless { it.isNullOrBlank() }

        val normalizedFeatures =
            place.features
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .toSet()

        val locationType = resolveLocationType(address, name)
        val placeType = PlaceType.OTHER
        val feeType = determineFeeType(name, normalizedFeatures)
        val accessibilityType = determineAccessibilityType(normalizedFeatures)
        val status = determineStatus(place.statusText, place.isOpenNow, normalizedFeatures)
        val amenities = buildAmenities(normalizedFeatures, locationType, place)
        val rawSchedule = buildRawSchedule(place.workingHours)

        val buildingContext =
            if (locationType == LocationType.INSIDE_BUILDING) {
                BuildingContext(
                    name = name,
                    address = address ?: name.orEmpty(),
                    workTime = rawSchedule,
                    externalId = providerObjectId
                )
            } else {
                null
            }

        return NormalizedRestroomCandidate(
            provider = provider,
            providerObjectId = providerObjectId,
            cityId = cityId,
            name = name,
            address = address,
            lat = place.location.lat,
            lng = place.location.lng,
            placeType = placeType,
            locationType = locationType,
            feeType = feeType,
            accessibilityType = accessibilityType,
            status = status,
            amenities = amenities,
            rawSchedule = rawSchedule,
            buildingContext = buildingContext,
            genderType = null
        )
    }

    private fun resolveLocationType(
        address: String?,
        name: String?
    ): LocationType {
        val source = listOfNotNull(address, name).joinToString(" ").lowercase()
        val buildingMarkers =
            listOf(
                "вокзал",
                "торговый центр",
                "торгово-развлекательный центр",
                "трц",
                "молл",
                "парк",
                "дворец культуры",
                "кинотеатр",
                "бизнес-центр"
            )
        return if (buildingMarkers.any { source.contains(it) }) {
            LocationType.INSIDE_BUILDING
        } else {
            LocationType.UNKNOWN
        }
    }

    private fun determineFeeType(
        name: String?,
        features: Set<String>
    ): FeeType {
        val source = (name ?: "").lowercase()
        return when {
            source.contains("платный") || features.any { it.contains("платный туалет") } -> FeeType.PAID
            source.contains("бесплатный") || features.any { it.contains("бесплатный туалет") } -> FeeType.FREE
            else -> FeeType.UNKNOWN
        }
    }

    private fun determineAccessibilityType(features: Set<String>): AccessibilityType {
        val hasWheelchairToilet =
            features.any { it.contains("туалет для людей с инвалидностью") } ||
                features.any { it.contains("туалет для маломобильных людей") }
        val hasAccessibleEntrance =
            features.any { it.contains("доступно") } ||
                features.any { it.contains("пандус") } ||
                features.any { it.contains("кнопка вызова персонала") } ||
                features.any { it.contains("парковка для людей с инвалидностью") }
        return when {
            hasWheelchairToilet || hasAccessibleEntrance -> AccessibilityType.WHEELCHAIR
            else -> AccessibilityType.UNKNOWN
        }
    }

    private fun determineStatus(
        statusText: String?,
        isOpenNow: Boolean?,
        features: Set<String>
    ): RestroomStatus {
        val text = statusText?.lowercase().orEmpty()
        val accessLimited = features.any { it.contains("доступ ограничен") }
        return if (text.contains("закрыт") || isOpenNow == false || accessLimited) {
            RestroomStatus.INACTIVE
        } else {
            RestroomStatus.ACTIVE
        }
    }

    private fun buildAmenities(
        features: Set<String>,
        locationType: LocationType,
        place: YandexMapsScrapedPlace
    ): JsonObject {
        val accessibleEntrance =
            features.any { it.contains("доступно") } ||
                features.any { it.contains("пандус") }
        val accessibleToilet =
            features.any { it.contains("туалет для людей с инвалидностью") } ||
                features.any { it.contains("туалет для маломобильных людей") }

        return buildJsonObject {
            put("accessible_entrance", JsonPrimitive(accessibleEntrance))
            put("accessible_toilet", JsonPrimitive(accessibleToilet))
            put("access_limited", JsonPrimitive(features.any { it.contains("доступ ограничен") }))
            put("toilet_context", JsonPrimitive(locationType.name))
            put(
                "source_features",
                buildJsonArray {
                    place.features.forEach { add(JsonPrimitive(it)) }
                }
            )
        }
    }

    private fun buildRawSchedule(workingHours: List<yayauheny.by.model.import.yandex.YandexMapsScrapedWorkingHour>?): JsonObject? {
        if (workingHours.isNullOrEmpty()) return null
        return buildJsonObject {
            workingHours.forEach { wh ->
                put(
                    wh.day,
                    buildJsonObject {
                        put("from", JsonPrimitive(wh.from))
                        put("to", JsonPrimitive(wh.to))
                    }
                )
            }
        }
    }
}
