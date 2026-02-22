package yayauheny.by.service.import.twogis

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.repository.impl.SubwayRepositoryImpl
import yayauheny.by.service.import.ArrayOrSingleExtractor
import yayauheny.by.service.import.ImportObjectResult
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.PayloadExtractor
import yayauheny.by.service.import.RestroomCandidateMapper
import yayauheny.by.util.transactionSuspend

/**
 * Стратегия импорта данных из 2ГИС scraped формата.
 * Использует pipeline: extract -> parse -> normalize -> map -> upsert
 */
class TwoGisScrapedImportStrategy(
    private val dsl: DSLContext,
    private val restroomRepository: RestroomRepository
) : ImportStrategy {
    private val logger = LoggerFactory.getLogger(TwoGisScrapedImportStrategy::class.java)
    private val extractor: PayloadExtractor = ArrayOrSingleExtractor()
    private val parser = TwoGisScrapedParser()
    private val normalizer = TwoGisScrapedNormalizer()

    override fun provider(): ImportProvider = ImportProvider.TWO_GIS

    override suspend fun importObject(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject
    ): ImportObjectResult {
        val results = importBatch(cityId, payloadType, payload)
        return results.firstOrNull()
            ?: throw IllegalArgumentException("No items found in payload")
    }

    override suspend fun importBatch(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject
    ): List<ImportObjectResult> {
        if (payloadType != ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON) {
            throw IllegalArgumentException("Unsupported payload type: $payloadType")
        }

        val items = extractor.extractItems(payload)
        if (items.isEmpty()) {
            throw IllegalArgumentException("No items found in payload")
        }

        logger.info("Processing batch import: ${items.size} items")

        return dsl.transactionSuspend { txCtx ->
            val txRestroomRepo = RestroomRepositoryImpl(txCtx)
            val txSubwayRepo = SubwayRepositoryImpl(txCtx)

            items.mapIndexed { index, item ->
                try {
                    val place = parser.parse(item)
                    val candidate = normalizer.normalize(cityId, place, payloadType)
                    val createDto = RestroomCandidateMapper.toCreateDto(candidate)

                    val existingRestroom =
                        txRestroomRepo.findByOrigin(
                            originProvider = createDto.originProvider,
                            originId = createDto.originId!!
                        )

                    val restroomId =
                        if (existingRestroom != null) {
                            // Обновляем существующий
                            val updateDto =
                                RestroomUpdateDto(
                                    cityId = createDto.cityId,
                                    buildingId = createDto.buildingId,
                                    subwayStationId = createDto.subwayStationId,
                                    name = createDto.name,
                                    address = createDto.address,
                                    phones = createDto.phones,
                                    workTime = createDto.workTime,
                                    feeType = createDto.feeType,
                                    genderType = createDto.genderType,
                                    accessibilityType = createDto.accessibilityType,
                                    placeType = createDto.placeType,
                                    coordinates = createDto.coordinates,
                                    status = createDto.status,
                                    amenities = createDto.amenities,
                                    externalMaps = createDto.externalMaps,
                                    accessNote = createDto.accessNote,
                                    directionGuide = createDto.directionGuide,
                                    inheritBuildingSchedule = createDto.inheritBuildingSchedule,
                                    hasPhotos = createDto.hasPhotos,
                                    locationType = createDto.locationType,
                                    originProvider = createDto.originProvider,
                                    originId = createDto.originId,
                                    isHidden = createDto.isHidden
                                )
                            txRestroomRepo.update(existingRestroom.id, updateDto).id
                        } else {
                            // Создаем новый
                            txRestroomRepo.save(createDto).id
                        }

                    // Устанавливаем ближайшую станцию метро
                    txSubwayRepo.setNearestStationForRestroom(
                        restroomId = restroomId,
                        lat = place.location.lat,
                        lon = place.location.lng
                    )

                    logger.debug("Successfully imported item {}: restroomId={}", index, restroomId)
                    ImportObjectResult(
                        restroomId = restroomId,
                        buildingId = null
                    )
                } catch (e: Exception) {
                    logger.error("Failed to import item $index: ${e.message}", e)
                    throw e // Пробрасываем ошибку для обработки в сервисе
                }
            }
        }
    }
}
