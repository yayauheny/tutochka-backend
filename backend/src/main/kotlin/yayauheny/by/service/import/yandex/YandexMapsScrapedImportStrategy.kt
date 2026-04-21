package yayauheny.by.service.import.yandex

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.impl.BuildingRepositoryImpl
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.repository.impl.SubwayRepositoryImpl
import yayauheny.by.service.import.ArrayOrSingleExtractor
import yayauheny.by.service.import.ImportObjectResult
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.InvalidImportPayload
import yayauheny.by.service.import.PayloadExtractor
import yayauheny.by.service.import.RestroomCandidateMapper
import yayauheny.by.service.import.UnsupportedPayloadType
import yayauheny.by.util.transactionSuspend

class YandexMapsScrapedImportStrategy(
    private val dsl: DSLContext,
    private val restroomRepository: RestroomRepository
) : ImportStrategy {
    private val logger = LoggerFactory.getLogger(YandexMapsScrapedImportStrategy::class.java)
    private val extractor: PayloadExtractor = ArrayOrSingleExtractor()
    private val parser = YandexMapsScrapedParser()
    private val normalizer = YandexMapsScrapedNormalizer()

    override fun provider(): ImportProvider = ImportProvider.YANDEX_MAPS

    override suspend fun importObject(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject
    ): ImportObjectResult {
        val results = importBatch(cityId, payloadType, payload)
        return results.firstOrNull()
            ?: throw InvalidImportPayload("No items found in payload")
    }

    override suspend fun importBatch(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject,
        tx: DSLContext?
    ): List<ImportObjectResult> {
        if (payloadType != ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON) {
            throw UnsupportedPayloadType(provider(), payloadType)
        }

        val items = extractor.extractItems(payload)
        if (items.isEmpty()) {
            throw InvalidImportPayload("No items found in payload")
        }

        logger.info("Processing Yandex batch import: ${items.size} items")

        return if (tx != null) {
            runInTx(tx, cityId, payloadType, items)
        } else {
            dsl.transactionSuspend { runInTx(it, cityId, payloadType, items) }
        }
    }

    private suspend fun runInTx(
        txCtx: DSLContext,
        cityId: UUID,
        payloadType: ImportPayloadType,
        items: List<JsonObject>
    ): List<ImportObjectResult> {
        val txBuildingRepo = BuildingRepositoryImpl(txCtx)
        val txRestroomRepo = RestroomRepositoryImpl(txCtx)
        val txSubwayRepo = SubwayRepositoryImpl(txCtx)

        return items.mapIndexed { index, item ->
            val place = parser.parse(item)
            val candidate = normalizer.normalize(cityId, place, payloadType)
            val (createDto, buildingId) = resolveBuildingAndCreateDto(candidate, txBuildingRepo)

            val originId =
                requireNotNull(createDto.originId) {
                    "originId is required for provider=${candidate.provider}"
                }

            val existingRestroom =
                txRestroomRepo.findByOrigin(
                    originProvider = createDto.originProvider,
                    originId = originId
                )

            val restroomId =
                if (existingRestroom != null) {
                    val updateDto = toUpdateDto(createDto)
                    txRestroomRepo.update(existingRestroom.id, updateDto).id
                } else {
                    txRestroomRepo.save(createDto).id
                }

            txSubwayRepo.setNearestStationForRestroom(
                restroomId = restroomId,
                lat = candidate.lat,
                lon = candidate.lng
            )

            logger.debug(
                "Successfully imported Yandex item {}: restroomId={}, buildingId={}",
                index,
                restroomId,
                buildingId
            )

            ImportObjectResult(
                restroomId = restroomId,
                buildingId = buildingId
            )
        }
    }

    private suspend fun resolveBuildingAndCreateDto(
        candidate: NormalizedRestroomCandidate,
        buildingRepo: BuildingRepositoryImpl
    ): Pair<RestroomCreateDto, UUID?> {
        val ctx = candidate.buildingContext
        if (ctx == null) {
            return RestroomCandidateMapper.toCreateDto(candidate) to null
        }

        val existing = buildingRepo.findByExternalId(provider = "yandex", externalId = ctx.externalId)
        val building =
            existing
                ?: buildingRepo.save(
                    yayauheny.by.model.building.BuildingCreateDto(
                        cityId = candidate.cityId,
                        name = ctx.name,
                        address = ctx.address,
                        buildingType = candidate.placeType,
                        workTime = ctx.workTime,
                        coordinates = Coordinates(lat = candidate.lat, lon = candidate.lng),
                        externalIds =
                            kotlinx.serialization.json.buildJsonObject {
                                put("yandex", kotlinx.serialization.json.JsonPrimitive(ctx.externalId))
                            }
                    )
                )

        val createDto =
            RestroomCandidateMapper.toCreateDto(
                candidate,
                buildingId = building.id,
                inheritBuildingSchedule = true
            )
        return createDto to building.id
    }

    private fun toUpdateDto(createDto: RestroomCreateDto): RestroomUpdateDto =
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
}
