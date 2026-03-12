package yayauheny.by.service.import.twogis

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import yayauheny.by.model.building.BuildingCreateDto
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
import yayauheny.by.service.import.PayloadExtractor
import yayauheny.by.service.import.InvalidImportPayload
import yayauheny.by.service.import.RestroomCandidateMapper
import yayauheny.by.service.import.UnsupportedPayloadType
import yayauheny.by.util.transactionSuspend

/**
 * Стратегия импорта данных из 2ГИС scraped формата.
 * Pipeline: extract -> parse -> normalize -> [resolve building] -> map -> upsert.
 * При locationType == INSIDE_BUILDING сначала создаётся/находится здание по external_id 2ГИС, затем туалет привязывается к зданию с наследованием расписания.
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
            ?: throw InvalidImportPayload("No items found in payload")
    }

    override suspend fun importBatch(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject,
        tx: DSLContext?
    ): List<ImportObjectResult> {
        if (payloadType != ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON) {
            throw UnsupportedPayloadType(provider(), payloadType)
        }

        val items = extractor.extractItems(payload)
        if (items.isEmpty()) {
            throw InvalidImportPayload("No items found in payload")
        }

        logger.info("Processing batch import: ${items.size} items")

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
                    txRestroomRepo.save(createDto).id
                }

            txSubwayRepo.setNearestStationForRestroom(
                restroomId = restroomId,
                lat = place.location.lat,
                lon = place.location.lng
            )

            logger.debug("Successfully imported item {}: restroomId={}, buildingId={}", index, restroomId, buildingId)
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

        val existing = buildingRepo.findByExternalId(provider = "2gis", externalId = ctx.externalId)
        val building =
            existing
                ?: buildingRepo.save(
                    BuildingCreateDto(
                        cityId = candidate.cityId,
                        name = ctx.name,
                        address = ctx.address,
                        buildingType = candidate.placeType,
                        workTime = ctx.workTime,
                        coordinates = Coordinates(lat = candidate.lat, lon = candidate.lng),
                        externalIds = buildJsonObject { put("2gis", JsonPrimitive(ctx.externalId)) }
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
}
