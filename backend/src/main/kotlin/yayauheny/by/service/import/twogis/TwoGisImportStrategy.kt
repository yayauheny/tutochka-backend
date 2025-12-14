package yayauheny.by.service.import.twogis

import yayauheny.by.model.dto.LatLon
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.import.BuildingImportStatus
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportProvider
import yayauheny.by.model.import.twogis.TwoGisResponse
import yayauheny.by.model.import.twogis.TwoGisAttributeGroup
import yayauheny.by.model.import.twogis.TwoGisPlace
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.BuildingRepository
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.SubwayRepository
import yayauheny.by.repository.impl.BuildingRepositoryImpl
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.repository.impl.SubwayRepositoryImpl
import yayauheny.by.service.import.ImportObjectResult
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.util.transactionSuspend

/**
 * Стратегия импорта данных из 2ГИС.
 * Парсит JSON ответ 2ГИС и создает/обновляет здания и туалеты.
 */
class TwoGisImportStrategy(
    private val dsl: DSLContext
) : ImportStrategy {
    private val logger = LoggerFactory.getLogger(TwoGisImportStrategy::class.java)
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    override fun provider(): ImportProvider = ImportProvider.TWO_GIS

    override suspend fun importObject(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject
    ): ImportObjectResult {
        val place = parsePlace(payload, payloadType)
        val tags = collectTags(place.attributeGroups)

        return dsl.transactionSuspend { txCtx ->
            val txRepos =
                TxRepos(
                    building = BuildingRepositoryImpl(txCtx),
                    restroom = RestroomRepositoryImpl(txCtx),
                    subway = SubwayRepositoryImpl(txCtx)
                )

            val buildingId =
                place.address.buildingId?.let { buildingIdStr ->
                    upsertBuilding(txRepos.building, cityId, place, buildingIdStr)
                }

            val restroomId = upsertRestroom(txRepos.restroom, cityId, place, buildingId, tags)

            txRepos.subway.setNearestStationForRestroom(
                restroomId = restroomId,
                lat = place.point.lat,
                lon = place.point.lon
            )

            ImportObjectResult(
                restroomId = restroomId,
                buildingId = buildingId
            )
        }
    }

    /**
     * Парсит место из JSON payload.
     * Поддерживает как единичный item, так и полный ответ с result.items[0].
     */
    private fun parsePlace(
        payload: JsonObject,
        payloadType: ImportPayloadType
    ): TwoGisPlace {
        return runCatching {
            when (payloadType) {
                ImportPayloadType.TWO_GIS_PLACE_JSON -> {
                    if (payload.containsKey("result")) {
                        val response = json.decodeFromJsonElement(TwoGisResponse.serializer(), payload)
                        response.result.items.firstOrNull()
                            ?: error("Invalid 2GIS response structure: result.items is empty")
                    } else {
                        json.decodeFromJsonElement(TwoGisPlace.serializer(), payload)
                    }
                }
            }
        }.getOrElse { e ->
            val payloadPreview =
                runCatching { json.encodeToString(JsonObject.serializer(), payload) }
                    .getOrElse { payload.toString() }
                    .take(1000)

            logger.error(
                "Failed to parse 2GIS payload (type={}): {}",
                payloadType,
                payloadPreview,
                e
            )
            throw IllegalArgumentException("Failed to parse 2GIS payload: ${e.message}", e)
        }
    }

    /**
     * Создает или обновляет здание на основе данных 2ГИС.
     */
    private suspend fun upsertBuilding(
        repo: BuildingRepository,
        cityId: UUID,
        place: TwoGisPlace,
        buildingIdStr: String
    ): UUID? {
        val existingBuilding = repo.findByExternalId("2gis", buildingIdStr)

        val externalIds = buildExternalIds("2gis", buildingIdStr)

        val coordinates =
            LatLon(
                lat = place.point.lat,
                lon = place.point.lon
            )

        return if (existingBuilding != null) {
            existingBuilding.id
        } else {
            val createDto =
                BuildingCreateDto(
                    cityId = cityId,
                    name = null,
                    address = place.addressName,
                    buildingType = null,
                    workTime = null,
                    coordinates = coordinates,
                    externalIds = externalIds,
                    importStatus = BuildingImportStatus.PENDING_DETAILS
                )
            repo.save(createDto).id
        }
    }

    /**
     * Создает или обновляет туалет на основе данных 2ГИС.
     */
    private suspend fun upsertRestroom(
        repo: RestroomRepository,
        cityId: UUID,
        place: TwoGisPlace,
        buildingId: UUID?,
        tags: Set<String>
    ): UUID {
        val existingRestroom = repo.findByExternalMap("2gis", place.id)

        val externalMaps = buildExternalIds("2gis", place.id)

        val coordinates =
            LatLon(
                lat = place.point.lat,
                lon = place.point.lon
            )

        val feeType = mapFeeType(tags)
        val accessibilityType = mapAccessibilityType(tags)
        val amenities = mapAmenities(tags)
        val workTime = place.schedule
        val hasPhotos = place.flags?.photos ?: false

        val addressFull = place.addressComment?.let { "${place.addressName}, $it" } ?: place.addressName

        val createDto =
            RestroomCreateDto(
                cityId = cityId,
                buildingId = buildingId,
                subwayStationId = null, // Будет заполнено позже через SubwayBindingService
                status = RestroomStatus.ACTIVE,
                name = place.name,
                address = addressFull,
                phones = null,
                workTime = workTime,
                feeType = feeType,
                accessibilityType = accessibilityType,
                placeType = PlaceType.OTHER,
                coordinates = coordinates,
                dataSource = DataSourceType.IMPORT,
                amenities = amenities,
                externalMaps = externalMaps,
                accessNote = null,
                directionGuide = place.addressComment,
                inheritBuildingSchedule = false,
                hasPhotos = hasPhotos
            )

        return if (existingRestroom != null) {
            val updateDto =
                RestroomUpdateDto(
                    cityId = createDto.cityId,
                    name = createDto.name,
                    address = createDto.address,
                    phones = createDto.phones,
                    workTime = createDto.workTime,
                    feeType = createDto.feeType,
                    accessibilityType = createDto.accessibilityType,
                    placeType = createDto.placeType,
                    coordinates = createDto.coordinates,
                    amenities = createDto.amenities,
                    externalMaps = createDto.externalMaps,
                    accessNote = createDto.accessNote,
                    directionGuide = createDto.directionGuide,
                    inheritBuildingSchedule = createDto.inheritBuildingSchedule,
                    hasPhotos = createDto.hasPhotos,
                    status = createDto.status,
                    buildingId = createDto.buildingId,
                    subwayStationId = createDto.subwayStationId
                )
            repo.update(existingRestroom.id, updateDto).id
        } else {
            repo.save(createDto).id
        }
    }

    /**
     * Маппит теги 2ГИС на FeeType.
     */
    private fun mapFeeType(tags: Set<String>): FeeType {
        return when {
            tags.any { tag -> tag.contains("paid_toilet") || tag.contains("toilet_paid") } -> FeeType.PAID
            else -> FeeType.FREE
        }
    }

    /**
     * Маппит теги 2ГИС на AccessibilityType.
     */
    private fun mapAccessibilityType(tags: Set<String>): AccessibilityType {
        return when {
            tags.any { tag -> tag.contains("wc_inclusive") || tag.contains("accessible") } -> AccessibilityType.DISABLED
            tags.any { tag -> tag.contains("men") || tag.contains("мужской") } -> AccessibilityType.MEN
            tags.any { tag -> tag.contains("women") || tag.contains("женский") } -> AccessibilityType.WOMEN
            tags.any { tag -> tag.contains("family") || tag.contains("семейный") } -> AccessibilityType.FAMILY
            tags.any { tag -> tag.contains("unisex") || tag.contains("универсальный") } -> AccessibilityType.UNISEX
            else -> AccessibilityType.UNISEX // По умолчанию
        }
    }

    /**
     * Маппит теги 2ГИС на amenities JSONB.
     */
    private fun mapAmenities(tags: Set<String>): JsonObject {
        val paidToilet = tags.any { tag -> tag.contains("paid_toilet") || tag.contains("toilet_paid") }
        val paymentCard = tags.any { tag -> tag.contains("payment_type_card") || tag.contains("card") }
        val accessibleWc = tags.any { tag -> tag.contains("wc_inclusive") || tag.contains("accessible") }
        return buildJsonObject {
            put("paid_toilet", JsonPrimitive(paidToilet))
            put("payment_card", JsonPrimitive(paymentCard))
            put("accessible_wc", JsonPrimitive(accessibleWc))
        }
    }

    private fun buildExternalIds(
        provider: String,
        id: String
    ): JsonObject =
        buildJsonObject {
            put(provider, JsonPrimitive(id))
        }

    private fun collectTags(attributeGroups: List<TwoGisAttributeGroup>): Set<String> =
        attributeGroups
            .asSequence()
            .flatMap { it.attributes.asSequence() }
            .map { it.tag.lowercase() }
            .toSet()

    private data class TxRepos(
        val building: BuildingRepository,
        val restroom: RestroomRepository,
        val subway: SubwayRepository
    )
}
