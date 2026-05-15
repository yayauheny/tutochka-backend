package yayauheny.by.importing.provider.yandex

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import yayauheny.by.importing.provider.ProviderNormalizationProfile
import yayauheny.by.importing.provider.ProviderResolvedFields
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.import.BuildingContext

internal object YandexNormalizationProfile : ProviderNormalizationProfile<YandexMapsScrapedPlace> {
    override fun resolve(providerDto: YandexMapsScrapedPlace): ProviderResolvedFields {
        val name = resolveName(providerDto)
        val address = resolveAddress(providerDto)
        val normalizedFeatures = normalizeFeatures(providerDto)
        val locationType = resolveLocationType(address, name)
        val rawSchedule = resolveRawSchedule(providerDto)

        return ProviderResolvedFields(
            name = name,
            address = address,
            locationType = locationType,
            placeType = resolvePlaceType(providerDto),
            feeType = resolveFeeType(name, normalizedFeatures),
            accessibilityType = resolveAccessibilityType(normalizedFeatures),
            status = resolveStatus(providerDto.statusText, providerDto.isOpenNow, normalizedFeatures),
            amenities = resolveAmenities(normalizedFeatures, locationType, providerDto),
            rawSchedule = rawSchedule,
            buildingContext = resolveBuildingContext(providerDto, name, address, locationType, rawSchedule),
            genderType = null
        )
    }

    private fun resolveName(providerDto: YandexMapsScrapedPlace): String =
        providerDto.shortTitle?.trim().takeUnless { it.isNullOrBlank() }
            ?: providerDto.title?.trim().takeUnless { it.isNullOrBlank() }
            ?: "Туалет"

    private fun resolveAddress(providerDto: YandexMapsScrapedPlace): String? = providerDto.address?.trim().takeUnless { it.isNullOrBlank() }

    private fun normalizeFeatures(providerDto: YandexMapsScrapedPlace): Set<String> =
        providerDto.features
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .toSet()

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

    private fun resolvePlaceType(providerDto: YandexMapsScrapedPlace): PlaceType = PlaceType.OTHER

    private fun resolveFeeType(
        name: String?,
        features: Set<String>
    ): FeeType {
        val source = (name ?: "").lowercase()
        return when {
            source.contains("бесплатный") || features.any { it.contains("бесплатный туалет") } -> FeeType.FREE
            source.contains("платный") || features.any { it.contains("платный туалет") } -> FeeType.PAID
            else -> FeeType.UNKNOWN
        }
    }

    private fun resolveAccessibilityType(features: Set<String>): AccessibilityType {
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

    private fun resolveStatus(
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

    private fun resolveAmenities(
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

    private fun resolveRawSchedule(providerDto: YandexMapsScrapedPlace): JsonObject? {
        val workingHours = providerDto.workingHours ?: return null
        if (workingHours.isEmpty()) return null
        return buildJsonObject {
            workingHours.forEach { wh ->
                putJsonObject(wh.day) {
                    put("from", JsonPrimitive(wh.from))
                    put("to", JsonPrimitive(wh.to))
                    if (wh.intervals.isNotEmpty()) {
                        putJsonArray("intervals") {
                            wh.intervals.forEach { interval ->
                                add(
                                    buildJsonObject {
                                        put("from", JsonPrimitive(interval.from))
                                        put("to", JsonPrimitive(interval.to))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resolveBuildingContext(
        providerDto: YandexMapsScrapedPlace,
        name: String?,
        address: String?,
        locationType: LocationType,
        rawSchedule: JsonObject?
    ): BuildingContext? {
        if (locationType != LocationType.INSIDE_BUILDING) {
            return null
        }

        return BuildingContext(
            name = name,
            address = address ?: name.orEmpty(),
            workTime = rawSchedule,
            externalId = providerDto.placeId
        )
    }
}
