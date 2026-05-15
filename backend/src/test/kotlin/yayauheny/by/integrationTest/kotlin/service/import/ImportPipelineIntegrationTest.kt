package yayauheny.by.integrationTest.kotlin.importing

import integration.base.BaseIntegrationTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.importing.provider.twogis.TwoGisImportAdapter
import yayauheny.by.importing.provider.yandex.YandexImportAdapter
import yayauheny.by.importing.repository.BuildingImportRepositoryImpl
import yayauheny.by.importing.repository.DuplicateSuspicionRepositoryImpl
import yayauheny.by.importing.repository.ImportInboxRepositoryImpl
import yayauheny.by.importing.repository.RestroomImportRepositoryImpl
import yayauheny.by.importing.service.ImportAdapterRegistry
import yayauheny.by.importing.service.ImportBatchProcessor
import yayauheny.by.importing.service.ImportCityResolver
import yayauheny.by.importing.service.ImportPipeline
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.importing.model.ImportEntityType
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.model.import.ImportStatus
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.transactionSuspend

@Tag("integration")
@DisplayName("ImportPipeline Integration Tests")
class ImportPipelineIntegrationTest : BaseIntegrationTest() {
    private lateinit var importBatchProcessor: ImportBatchProcessor
    private lateinit var importPipeline: ImportPipeline
    private lateinit var restroomRepository: RestroomRepositoryImpl

    private val suspicionTable = DSL.table(DSL.name("restroom_duplicate_suspicions"))
    private val suspicionExistingRestroomId = DSL.field(DSL.name("existing_restroom_id"), SQLDataType.UUID.nullable(false))
    private val suspicionCandidateRestroomId = DSL.field(DSL.name("candidate_restroom_id"), SQLDataType.UUID.nullable(false))
    private val suspicionDistanceMeters = DSL.field(DSL.name("distance_m"), SQLDataType.DOUBLE.nullable(false))

    @BeforeEach
    override fun openConnectionAndResetData() {
        super.openConnectionAndResetData()
        restroomRepository = RestroomRepositoryImpl(dslContext)
        val registry =
            ImportAdapterRegistry(
                listOf<ImportSourceAdapter>(
                    TwoGisImportAdapter(),
                    YandexImportAdapter()
                )
            )
        val importPipeline =
            ImportPipeline(
                buildingImportRepository = BuildingImportRepositoryImpl(dslContext),
                restroomImportRepository = RestroomImportRepositoryImpl(dslContext),
                duplicateSuspicionRepository = DuplicateSuspicionRepositoryImpl()
            )
        this.importPipeline = importPipeline
        importBatchProcessor =
            ImportBatchProcessor(
                ctx = dslContext,
                registry = registry,
                importCityResolver = ImportCityResolver(dslContext),
                importInboxRepository = ImportInboxRepositoryImpl(dslContext),
                importPipeline = importPipeline
            )
    }

    @Test
    fun `mixed-validity batch commits successes and returns failures`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val payload =
                buildJsonObject {
                    put(
                        "items",
                        buildJsonArray {
                            add(
                                validStandaloneItem(id = "2gis_valid", title = "Valid Toilet", address = "Street 1", lat = 53.9, lng = 27.5)
                            )
                            add(
                                buildJsonObject {
                                    put("id", "2gis_invalid")
                                }
                            )
                        }
                    )
                }

            val results =
                importBatchProcessor
                    .process(
                        provider = ImportProvider.TWO_GIS,
                        payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                        cityId = env.cityId,
                        items = payload["items"]!!.let { it as kotlinx.serialization.json.JsonArray }.map { it as JsonObject },
                        chunkSize = 50
                    ).results

            assertEquals(2, results.size)
            assertEquals(ImportStatus.CREATED, results[0].outcome)
            assertNotNull(results[0].restroomId)
            assertEquals(ImportStatus.FAILED, results[1].outcome)
            assertEquals("INVALID_PAYLOAD", results[1].errorCode)

            val restroomCount =
                dslContext
                    .selectCount()
                    .from(RESTROOMS)
                    .where(RESTROOMS.ORIGIN_PROVIDER.eq(ImportProvider.TWO_GIS.name))
                    .fetchOne(0, Int::class.java) ?: 0

            assertEquals(1, restroomCount)
        }

    @Test
    fun `repeated same-provider import updates same restroom`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)

            val first =
                validStandaloneItem(
                    id = "2gis_same",
                    title = "Original Name",
                    address = "Original Address",
                    lat = 53.9,
                    lng = 27.5
                )
            val second =
                validStandaloneItem(
                    id = "2gis_same",
                    title = "Updated Name",
                    address = "Updated Address",
                    lat = 53.9,
                    lng = 27.5
                )

            val firstResult = importSingle(env.cityId, first)
            val secondResult = importSingle(env.cityId, second)

            assertEquals(ImportStatus.CREATED, firstResult.outcome)
            assertEquals(ImportStatus.UPDATED, secondResult.outcome)
            assertEquals(firstResult.restroomId, secondResult.restroomId)

            val restroom = restroomRepository.findById(requireNotNull(secondResult.restroomId))
            assertNotNull(restroom)
            assertEquals("Updated Name", restroom.name)
            assertEquals("Updated Address", restroom.address)
        }

    @Test
    fun `exact building match links existing building`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val buildingId =
                DatabaseTestHelper.insertTestBuilding(
                    dslContext = dslContext,
                    cityId = env.cityId,
                    address = "Mall Street 1",
                    name = "Existing Mall",
                    lat = 53.9,
                    lon = 27.5
                )

            val payload =
                validInsideBuildingItem(
                    id = "2gis_building",
                    title = "Mall Toilet",
                    address = "Mall Street 1",
                    lat = 53.9,
                    lng = 27.5
                )

            val result = importSingle(env.cityId, payload)

            assertEquals(ImportStatus.CREATED, result.outcome)
            assertEquals(buildingId, result.buildingId)

            val externalId =
                dslContext
                    .select(BUILDINGS.EXTERNAL_IDS)
                    .from(BUILDINGS)
                    .where(BUILDINGS.ID.eq(buildingId))
                    .fetchOne(BUILDINGS.EXTERNAL_IDS)
                    ?.data()

            assertTrue(externalId?.contains("2gis_building") == true)
        }

    @Test
    fun `exact restroom match links existing restroom`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val existingRestroomId =
                DatabaseTestHelper.insertStandaloneRestroom(
                    dslContext = dslContext,
                    cityId = env.cityId,
                    name = "Green Time Toilet",
                    address = "Mall Street 1",
                    lat = 53.9,
                    lon = 27.5
                )

            val payload =
                validStandaloneItem(
                    id = "2gis_duplicate",
                    title = "Green Time Toilet",
                    address = "Mall Street 1",
                    lat = 53.9,
                    lng = 27.5
                )

            val result = importSingle(env.cityId, payload)

            assertEquals(ImportStatus.LINKED_DUPLICATE, result.outcome)
            assertEquals(existingRestroomId, result.restroomId)
            assertEquals(existingRestroomId, result.duplicateOfRestroomId)

            val restroomCount =
                dslContext
                    .selectCount()
                    .from(RESTROOMS)
                    .where(RESTROOMS.CITY_ID.eq(env.cityId))
                    .fetchOne(0, Int::class.java) ?: 0
            assertEquals(1, restroomCount)
        }

    @Test
    fun `ambiguous nearby restroom inserts separately and creates suspicion`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val existingRestroomId =
                DatabaseTestHelper.insertStandaloneRestroom(
                    dslContext = dslContext,
                    cityId = env.cityId,
                    name = "Existing Toilet",
                    address = "Street A 1",
                    lat = 53.9,
                    lon = 27.5
                )

            val payload =
                validStandaloneItem(
                    id = "2gis_nearby",
                    title = "New Toilet",
                    address = "Street B 2",
                    lat = 53.90008,
                    lng = 27.50008
                )

            val result = importSingle(env.cityId, payload)

            assertEquals(ImportStatus.CREATED, result.outcome)
            assertNotNull(result.restroomId)
            assertTrue(result.restroomId != existingRestroomId)

            val suspicionCount =
                dslContext
                    .selectCount()
                    .from(suspicionTable)
                    .where(suspicionExistingRestroomId.eq(existingRestroomId))
                    .and(suspicionCandidateRestroomId.eq(result.restroomId))
                    .fetchOne(0, Int::class.java) ?: 0

            assertEquals(1, suspicionCount)
        }

    @Test
    fun `nearby duplicate suspicions keep only 3 nearest restrooms within 15 meters`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val candidateLat = 53.9
            val candidateLon = 27.5

            listOf(
                Triple("Nearby Toilet 1", candidateLat + 0.00001, candidateLon),
                Triple("Nearby Toilet 2", candidateLat + 0.00003, candidateLon),
                Triple("Nearby Toilet 3", candidateLat + 0.00006, candidateLon),
                Triple("Nearby Toilet 4", candidateLat + 0.00009, candidateLon),
                Triple("Far Toilet", candidateLat + 0.00200, candidateLon)
            ).forEachIndexed { index, restroom ->
                DatabaseTestHelper.insertStandaloneRestroom(
                    dslContext = dslContext,
                    cityId = env.cityId,
                    name = restroom.first,
                    address = "Street ${index + 1}",
                    lat = restroom.second,
                    lon = restroom.third
                )
            }

            val payload =
                validStandaloneItem(
                    id = "2gis_nearby_limit",
                    title = "Candidate Toilet",
                    address = "Candidate Street 99",
                    lat = candidateLat,
                    lng = candidateLon
                )

            val result = importSingle(env.cityId, payload)

            assertEquals(ImportStatus.CREATED, result.outcome)
            assertNotNull(result.restroomId)

            val suspicionDistances =
                dslContext
                    .select(suspicionDistanceMeters)
                    .from(suspicionTable)
                    .where(suspicionCandidateRestroomId.eq(result.restroomId))
                    .orderBy(suspicionDistanceMeters.asc())
                    .fetch(suspicionDistanceMeters)

            assertEquals(3, suspicionDistances.size)
            assertTrue(suspicionDistances.all { it <= 15.0 })
        }

    @Test
    fun `concurrent exact-match imports from different providers create one restroom and link both maps`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val name = "Cross Provider Toilet"
            val address = "Shared Street 42"
            val lat = 53.91
            val lon = 27.61

            val firstEnvelope =
                exactMatchEnvelope(
                    provider = ImportProvider.TWO_GIS,
                    providerObjectId = "2gis_cross_provider",
                    cityId = env.cityId,
                    name = name,
                    address = address,
                    lat = lat,
                    lng = lon
                )
            val secondEnvelope =
                exactMatchEnvelope(
                    provider = ImportProvider.YANDEX_MAPS,
                    providerObjectId = "yandex_cross_provider",
                    cityId = env.cityId,
                    name = name,
                    address = address,
                    lat = lat,
                    lng = lon
                )

            val results =
                coroutineScope {
                    listOf(firstEnvelope, secondEnvelope)
                        .map { envelope ->
                            async(Dispatchers.IO) {
                                dslContext.transactionSuspend { txCtx ->
                                    importPipeline.importEnvelopeInTx(txCtx, envelope)
                                }
                            }
                        }.awaitAll()
                }

            assertEquals(1, results.mapNotNull { it.restroomId }.toSet().size)
            assertTrue(results.any { it.outcome == ImportStatus.CREATED })
            assertTrue(results.any { it.outcome == ImportStatus.LINKED_DUPLICATE })

            val restroomCount =
                dslContext
                    .selectCount()
                    .from(RESTROOMS)
                    .where(RESTROOMS.CITY_ID.eq(env.cityId))
                    .and(RESTROOMS.NAME.eq(name))
                    .fetchOne(0, Int::class.java) ?: 0
            assertEquals(1, restroomCount)

            val restroomId = results.mapNotNull { it.restroomId }.first()
            val externalMaps =
                restroomRepository
                    .findById(restroomId)
                    ?.externalMaps

            assertNotNull(externalMaps)
            assertTrue(externalMaps!!.containsKey("2gis"))
            assertTrue(externalMaps.containsKey("yandex"))
        }

    @Test
    fun `parallel imports of same provider object produce one restroom`() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            val repository = RestroomRepositoryImpl(dslContext)
            val createDto =
                RestroomCreateDto(
                    cityId = env.cityId,
                    buildingId = null,
                    subwayStationId = null,
                    status = RestroomStatus.ACTIVE,
                    name = "Parallel Toilet",
                    address = "Parallel Street 1",
                    phones = null,
                    workTime = null,
                    feeType = FeeType.FREE,
                    genderType = GenderType.UNISEX,
                    accessibilityType = AccessibilityType.UNKNOWN,
                    placeType = PlaceType.PUBLIC,
                    coordinates = Coordinates(lat = 53.9, lon = 27.5),
                    dataSource = DataSourceType.IMPORT,
                    amenities = buildJsonObject { },
                    externalMaps =
                        buildJsonObject {
                            put("2gis", "2gis_parallel")
                        },
                    accessNote = null,
                    directionGuide = null,
                    inheritBuildingSchedule = false,
                    hasPhotos = false,
                    locationType = LocationType.STANDALONE,
                    originProvider = ImportProvider.TWO_GIS,
                    originId = "2gis_parallel",
                    isHidden = false
                )
            val matchKey =
                MatchKeyGenerator.restroomMatchKey(
                    cityId = env.cityId,
                    buildingId = null,
                    address = createDto.address,
                    name = createDto.name,
                    lat = createDto.coordinates.lat,
                    lon = createDto.coordinates.lon,
                    locationType = createDto.locationType
                )

            val results =
                coroutineScope {
                    List(2) {
                        async(Dispatchers.IO) {
                            repository.upsertImportedRestroom(createDto, matchKey)
                        }
                    }.awaitAll()
                }

            val restroomIds = results.map { it.restroom.id }.toSet()
            assertEquals(1, restroomIds.size)

            val storedCount =
                dslContext
                    .selectCount()
                    .from(RESTROOMS)
                    .where(RESTROOMS.ORIGIN_PROVIDER.eq(ImportProvider.TWO_GIS.name))
                    .and(RESTROOMS.ORIGIN_ID.eq("2gis_parallel"))
                    .fetchOne(0, Int::class.java) ?: 0

            assertEquals(1, storedCount)
        }

    private suspend fun importSingle(
        cityId: java.util.UUID,
        payload: JsonObject
    ) = importBatchProcessor
        .process(
            provider = ImportProvider.TWO_GIS,
            payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
            cityId = cityId,
            items = listOf(payload),
            chunkSize = 1
        ).results
        .single()

    private fun validStandaloneItem(
        id: String,
        title: String,
        address: String,
        lat: Double,
        lng: Double
    ) = buildJsonObject {
        put("id", id)
        put("title", title)
        put("category", "toilet")
        put("address", address)
        put("url", "https://2gis.by/minsk/firm/$id")
        put("scrapedAt", "2025-12-24T09:25:00.392Z")
        put(
            "location",
            buildJsonObject {
                put("lat", lat)
                put("lng", lng)
            }
        )
    }

    private fun validInsideBuildingItem(
        id: String,
        title: String,
        address: String,
        lat: Double,
        lng: Double
    ) = buildJsonObject {
        put("id", id)
        put("title", title)
        put("category", "mall")
        put("address", address)
        put(
            "location",
            buildJsonObject {
                put("lat", lat)
                put("lng", lng)
            }
        )
        put(
            "attributeGroups",
            buildJsonArray {
                add(JsonPrimitive("Туалет"))
            }
        )
    }

    private fun exactMatchEnvelope(
        provider: ImportProvider,
        providerObjectId: String,
        cityId: java.util.UUID,
        name: String,
        address: String,
        lat: Double,
        lng: Double
    ): ProviderImportEnvelope =
        ProviderImportEnvelope(
            inboxMetadata =
                InboxMetadata(
                    provider = provider,
                    entityType = ImportEntityType.PLACE,
                    externalId = providerObjectId,
                    sourceUrl = null,
                    scrapedAt = null,
                    payloadHash = "test-$providerObjectId"
                ),
            command =
                NormalizedRestroomCandidate(
                    provider = provider,
                    providerObjectId = providerObjectId,
                    cityId = cityId,
                    name = name,
                    address = address,
                    lat = lat,
                    lng = lng,
                    placeType = PlaceType.PUBLIC,
                    locationType = LocationType.STANDALONE,
                    feeType = FeeType.FREE,
                    accessibilityType = AccessibilityType.UNKNOWN,
                    status = RestroomStatus.ACTIVE,
                    amenities = buildJsonObject { },
                    rawSchedule = null,
                    buildingContext = null,
                    genderType = GenderType.UNISEX
                ),
            rawPayload = buildJsonObject { put("id", providerObjectId) }
        )
}
