package yayauheny.by.importing.provider.yandex

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import yayauheny.by.importing.provider.normalizedProviderText
import yayauheny.by.importing.provider.ProviderNormalizationProfile
import yayauheny.by.importing.provider.ProviderResolvedFields
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.imports.BuildingContext

internal object YandexNormalizationProfile : ProviderNormalizationProfile<YandexMapsScrapedPlace> {
    private val mapper = jacksonObjectMapper()

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

    private fun resolveAddress(providerDto: YandexMapsScrapedPlace): String? = providerDto.address.normalizedProviderText()

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
    ): JsonNode {
        val accessibleEntrance =
            features.any { it.contains("доступно") } ||
                features.any { it.contains("пандус") }
        val accessibleToilet =
            features.any { it.contains("туалет для людей с инвалидностью") } ||
                features.any { it.contains("туалет для маломобильных людей") }

        val node = mapper.createObjectNode()
        node.put("accessible_entrance", accessibleEntrance)
        node.put("accessible_toilet", accessibleToilet)
        node.put("access_limited", features.any { it.contains("доступ ограничен") })
        node.put("toilet_context", locationType.name)
        val sourceFeaturesArray = node.putArray("source_features")
        place.features.forEach { sourceFeaturesArray.add(it) }
        return node
    }

    private fun resolveRawSchedule(providerDto: YandexMapsScrapedPlace): JsonNode? {
        val workingHours = providerDto.workingHours ?: return null
        if (workingHours.isEmpty()) return null
        
        val node = mapper.createObjectNode()
        workingHours.forEach { wh ->
            val dayNode = node.putObject(wh.day)
            dayNode.put("from", wh.from)
            dayNode.put("to", wh.to)
            if (wh.intervals.isNotEmpty()) {
                val intervalsArray = dayNode.putArray("intervals")
                wh.intervals.forEach { interval ->
                    val intervalNode = mapper.createObjectNode()
                    intervalNode.put("from", interval.from)
                    intervalNode.put("to", interval.to)
                    intervalsArray.add(intervalNode)
                }
            }
        }
        return node
    }

    private fun resolveBuildingContext(
        providerDto: YandexMapsScrapedPlace,
        name: String?,
        address: String?,
        locationType: LocationType,
        rawSchedule: JsonNode?
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
