package yayauheny.by.service.import.twogis

import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import by.yayauheny.shared.dto.LatLon
import by.yayauheny.shared.enums.AccessibilityType
import by.yayauheny.shared.enums.DataSourceType
import by.yayauheny.shared.enums.FeeType
import by.yayauheny.shared.enums.PlaceType
import by.yayauheny.shared.enums.RestroomStatus
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.import.BuildingImportStatus
import yayauheny.by.service.import.ImportObjectResult
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportProvider
import yayauheny.by.model.import.twogis.TwoGisAttributeGroup
import yayauheny.by.model.import.twogis.TwoGisPlace
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.BuildingRepository
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.SubwayBindingService

/**
 * Стратегия импорта данных из 2ГИС.
 * Парсит JSON ответ 2ГИС и создает/обновляет здания и туалеты.
 */
class TwoGisImportStrategy(
    private val buildingRepository: BuildingRepository,
    private val restroomRepository: RestroomRepository,
    private val subwayBindingService: SubwayBindingService
) : ImportStrategy {
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
        // Парсим JSON 2ГИС
        val place = parsePlace(payload, payloadType)

        // 1. Обрабатываем здание (если есть building_id)
        val buildingId =
            place.address.buildingId?.let { buildingIdStr ->
                upsertBuilding(cityId, place, buildingIdStr)
            }

        // 2. Обрабатываем туалет
        val restroomId = upsertRestroom(cityId, place, buildingId)

        // 3. Привязываем ближайшую станцию метро
        subwayBindingService.bindNearestSubwayStation(restroomId)

        return ImportObjectResult(
            restroomId = restroomId,
            buildingId = buildingId
        )
    }

    /**
     * Парсит место из JSON payload.
     * Поддерживает как единичный item, так и полный ответ с result.items[0].
     */
    private fun parsePlace(
        payload: JsonObject,
        payloadType: ImportPayloadType
    ): TwoGisPlace {
        return when (payloadType) {
            ImportPayloadType.TWO_GIS_PLACE_JSON -> {
                // Проверяем, это единичный item или полный ответ
                if (payload.containsKey("result")) {
                    // Полный ответ: {"result": {"items": [{"id": "...", ...}]}}
                    val result =
                        payload["result"]?.jsonObject
                            ?: error("Invalid 2GIS response structure: missing result")
                    val itemsElement =
                        result["items"]
                            ?: error("Invalid 2GIS response structure: missing result.items")

                    // Парсим массив items и берем первый элемент
                    val itemsArray = itemsElement.jsonArray
                    if (itemsArray.isEmpty()) {
                        error("Invalid 2GIS response structure: items array is empty")
                    }
                    json.decodeFromJsonElement(TwoGisPlace.serializer(), itemsArray.first().jsonObject)
                } else {
                    // Единичный item
                    json.decodeFromJsonElement(TwoGisPlace.serializer(), payload)
                }
            }
        }
    }

    /**
     * Создает или обновляет здание на основе данных 2ГИС.
     */
    private suspend fun upsertBuilding(
        cityId: UUID,
        place: TwoGisPlace,
        buildingIdStr: String
    ): UUID? {
        // Ищем существующее здание по external_ids['2gis']
        val existingBuilding = buildingRepository.findByExternalId("2gis", buildingIdStr)

        val externalIds =
            buildJsonObject {
                put("2gis", JsonPrimitive(buildingIdStr))
            }

        val coordinates =
            LatLon(
                lat = place.point.lat,
                lon = place.point.lon
            )

        return if (existingBuilding != null) {
            // Обновляем существующее здание (если нужно)
            // Пока просто возвращаем ID существующего
            existingBuilding.id
        } else {
            // Создаем новое здание
            val createDto =
                BuildingCreateDto(
                    cityId = cityId,
                    name = null, // Здание создается "по пути", имя пока неизвестно
                    address = place.addressName,
                    buildingType = null,
                    workTime = null,
                    coordinates = coordinates,
                    externalIds = externalIds,
                    importStatus = BuildingImportStatus.PENDING_DETAILS
                )
            buildingRepository.save(createDto).id
        }
    }

    /**
     * Создает или обновляет туалет на основе данных 2ГИС.
     */
    private suspend fun upsertRestroom(
        cityId: UUID,
        place: TwoGisPlace,
        buildingId: UUID?
    ): UUID {
        // Ищем существующий туалет по external_maps['2gis']
        val existingRestroom = restroomRepository.findByExternalMap("2gis", place.id)

        val externalMaps =
            buildJsonObject {
                put("2gis", JsonPrimitive(place.id))
            }

        val coordinates =
            LatLon(
                lat = place.point.lat,
                lon = place.point.lon
            )

        // Маппинг полей из 2ГИС
        val feeType = mapFeeType(place.attributeGroups)
        val accessibilityType = mapAccessibilityType(place.attributeGroups)
        val amenities = mapAmenities(place.attributeGroups)
        val workTime = place.schedule // schedule уже JsonObject, можно использовать как есть
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
            // Обновляем существующий туалет
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
            restroomRepository.update(existingRestroom.id, updateDto).id
        } else {
            // Создаем новый туалет
            restroomRepository.save(createDto).id
        }
    }

    /**
     * Маппит теги 2ГИС на FeeType.
     */
    private fun mapFeeType(attributeGroups: List<TwoGisAttributeGroup>): FeeType {
        val allTags = attributeGroups.flatMap { group -> group.attributes }.map { attr -> attr.tag.lowercase() }
        return when {
            allTags.any { tag -> tag.contains("paid_toilet") || tag.contains("toilet_paid") } -> FeeType.PAID
            else -> FeeType.FREE
        }
    }

    /**
     * Маппит теги 2ГИС на AccessibilityType.
     */
    private fun mapAccessibilityType(attributeGroups: List<TwoGisAttributeGroup>): AccessibilityType {
        val allTags = attributeGroups.flatMap { group -> group.attributes }.map { attr -> attr.tag.lowercase() }
        return when {
            allTags.any { tag -> tag.contains("wc_inclusive") || tag.contains("accessible") } -> AccessibilityType.DISABLED
            allTags.any { tag -> tag.contains("men") || tag.contains("мужской") } -> AccessibilityType.MEN
            allTags.any { tag -> tag.contains("women") || tag.contains("женский") } -> AccessibilityType.WOMEN
            allTags.any { tag -> tag.contains("family") || tag.contains("семейный") } -> AccessibilityType.FAMILY
            allTags.any { tag -> tag.contains("unisex") || tag.contains("универсальный") } -> AccessibilityType.UNISEX
            else -> AccessibilityType.UNISEX // По умолчанию
        }
    }

    /**
     * Маппит теги 2ГИС на amenities JSONB.
     */
    private fun mapAmenities(attributeGroups: List<TwoGisAttributeGroup>): JsonObject {
        val allTags = attributeGroups.flatMap { group -> group.attributes }.map { attr -> attr.tag.lowercase() }
        val paidToilet = allTags.any { tag -> tag.contains("paid_toilet") || tag.contains("toilet_paid") }
        val paymentCard = allTags.any { tag -> tag.contains("payment_type_card") || tag.contains("card") }
        val accessibleWc = allTags.any { tag -> tag.contains("wc_inclusive") || tag.contains("accessible") }
        return buildJsonObject {
            put("paid_toilet", JsonPrimitive(paidToilet))
            put("payment_card", JsonPrimitive(paymentCard))
            put("accessible_wc", JsonPrimitive(accessibleWc))
        }
    }
}
