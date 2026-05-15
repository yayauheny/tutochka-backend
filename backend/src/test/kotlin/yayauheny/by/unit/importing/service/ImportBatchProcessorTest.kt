package yayauheny.by.unit.importing.service

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import java.sql.Connection
import java.util.UUID
import java.util.concurrent.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jooq.DSLContext
import org.jooq.ConnectionCallable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import yayauheny.by.importing.exception.UnsupportedImportProvider
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.model.ImportEntityType
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.importing.model.SourceLocationHint
import yayauheny.by.importing.provider.ImportSourceAdapter
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.importing.repository.ImportInboxRepository
import yayauheny.by.importing.service.ImportAdapterRegistry
import yayauheny.by.importing.service.ImportBatchProcessor
import yayauheny.by.importing.service.ImportCityResolver
import yayauheny.by.importing.service.ImportPipeline
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.import.ImportStatus
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.util.transactionSuspend

@DisplayName("ImportBatchProcessor Tests")
class ImportBatchProcessorTest {
    private val ctx = mockk<DSLContext>()
    private val txCtx = mockk<DSLContext>(relaxed = true)
    private val connection = mockk<Connection>(relaxed = true)
    private val registry = mockk<ImportAdapterRegistry>()
    private val importCityResolver = mockk<ImportCityResolver>()
    private val importInboxRepository = mockk<ImportInboxRepository>()
    private val importPipeline = mockk<ImportPipeline>()
    private val adapter = mockk<ImportSourceAdapter>()

    private val processor =
        ImportBatchProcessor(
            ctx = ctx,
            registry = registry,
            importCityResolver = importCityResolver,
            importInboxRepository = importInboxRepository,
            importPipeline = importPipeline
        )

    @AfterEach
    fun tearDown() {
        unmockkStatic("yayauheny.by.util.TransactionExtensionsKt")
        clearAllMocks()
    }

    @Test
    fun `process rethrows CancellationException`() =
        runTest {
            stubTransactionRunner()
            every { registry.get(ImportProvider.TWO_GIS) } returns adapter
            every { adapter.parseEnvelope(any(), any()) } throws CancellationException("cancelled")

            assertFailsWith<CancellationException> {
                processor.process(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = UUID.randomUUID(),
                    items = listOf(buildJsonObject { put("id", "item-1") }),
                    chunkSize = 1
                )
            }
        }

    @Test
    fun `process rethrows unexpected RuntimeException`() =
        runTest {
            stubTransactionRunner()
            every { registry.get(ImportProvider.TWO_GIS) } returns adapter
            every { adapter.parseEnvelope(any(), any()) } throws IllegalStateException("boom")

            assertFailsWith<IllegalStateException> {
                processor.process(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = UUID.randomUUID(),
                    items = listOf(buildJsonObject { put("id", "item-1") }),
                    chunkSize = 1
                )
            }
        }

    @Test
    fun `process converts ImportException into failed item`() =
        runTest {
            stubTransactionRunner()
            every { registry.get(ImportProvider.TWO_GIS) } returns adapter

            val payload = buildJsonObject { put("id", "item-1") }
            val envelope = sampleEnvelope()
            val importId = UUID.randomUUID()

            every { adapter.parseEnvelope(payload, any()) } returns envelope
            every {
                importInboxRepository.upsertPendingInTx(
                    txCtx,
                    ImportProvider.TWO_GIS,
                    ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    any(),
                    envelope.inboxMetadata,
                    payload
                )
            } returns importId
            every { importInboxRepository.markFailedInTx(txCtx, importId, any()) } just Runs
            every { importPipeline.importEnvelopeInTx(txCtx, envelope) } throws UnsupportedImportProvider("bad-provider")

            val summary =
                processor.process(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = UUID.randomUUID(),
                    items = listOf(payload),
                    chunkSize = 1
                )

            assertEquals(1, summary.failed)
            assertEquals(ImportStatus.FAILED, summary.results.single().outcome)
            assertEquals("IMPORT_FAILED", summary.results.single().errorCode)
            verify(exactly = 1) { importInboxRepository.markFailedInTx(txCtx, importId, "Unsupported import provider: bad-provider") }
        }

    @Test
    fun `process converts PSQLException into failed item`() =
        runTest {
            stubTransactionRunner()
            every { registry.get(ImportProvider.TWO_GIS) } returns adapter

            val payload = buildJsonObject { put("id", "item-2") }
            val envelope = sampleEnvelope(providerObjectId = "item-2", payloadHash = "hash-item-2")
            val importId = UUID.randomUUID()
            val databaseError = mockk<PSQLException>()

            every { databaseError.sqlState } returns PSQLState.UNIQUE_VIOLATION.state
            every { databaseError.message } returns "duplicate key value violates unique constraint"
            every { adapter.parseEnvelope(payload, any()) } returns envelope
            every {
                importInboxRepository.upsertPendingInTx(
                    txCtx,
                    ImportProvider.TWO_GIS,
                    ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    any(),
                    envelope.inboxMetadata,
                    payload
                )
            } returns importId
            every { importInboxRepository.markFailedInTx(txCtx, importId, any()) } just Runs
            every { importPipeline.importEnvelopeInTx(txCtx, envelope) } throws databaseError

            val summary =
                processor.process(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = UUID.randomUUID(),
                    items = listOf(payload),
                    chunkSize = 1
                )

            assertEquals(1, summary.failed)
            assertEquals(ImportStatus.FAILED, summary.results.single().outcome)
            assertEquals("IMPORT_FAILED", summary.results.single().errorCode)
        }

    @Test
    fun `process resolves city once per source country city group`() =
        runTest {
            stubTransactionRunner()
            every { registry.get(ImportProvider.TWO_GIS) } returns adapter

            val resolvedCityId = UUID.randomUUID()
            val payloadOne = buildJsonObject { put("id", "item-1") }
            val payloadTwo = buildJsonObject { put("id", "item-2") }
            val sourceLocation = SourceLocationHint(country = "Беларусь", city = "Минск", lat = 53.9, lng = 27.5)
            val envelopeOne = sampleEnvelope(providerObjectId = "item-1", cityId = resolvedCityId)
            val envelopeTwo = sampleEnvelope(providerObjectId = "item-2", cityId = resolvedCityId)
            val importIdOne = UUID.randomUUID()
            val importIdTwo = UUID.randomUUID()
            val restroomIdOne = UUID.randomUUID()
            val restroomIdTwo = UUID.randomUUID()

            every { adapter.extractSourceLocation(payloadOne) } returns sourceLocation
            every { adapter.extractSourceLocation(payloadTwo) } returns sourceLocation
            coEvery {
                importCityResolver.resolveByMetadata(
                    countryName = "Беларусь",
                    cityName = "Минск"
                )
            } returns ImportCityResolver.MetadataCityResolution.Resolved(resolvedCityId)
            every {
                adapter.parseEnvelope(
                    payloadOne,
                    ImportAdapterContext(
                        payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                        cityId = resolvedCityId
                    )
                )
            } returns envelopeOne
            every {
                adapter.parseEnvelope(
                    payloadTwo,
                    ImportAdapterContext(
                        payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                        cityId = resolvedCityId
                    )
                )
            } returns envelopeTwo
            every {
                importInboxRepository.upsertPendingInTx(
                    txCtx,
                    ImportProvider.TWO_GIS,
                    ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    resolvedCityId,
                    envelopeOne.inboxMetadata,
                    payloadOne
                )
            } returns importIdOne
            every {
                importInboxRepository.upsertPendingInTx(
                    txCtx,
                    ImportProvider.TWO_GIS,
                    ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    resolvedCityId,
                    envelopeTwo.inboxMetadata,
                    payloadTwo
                )
            } returns importIdTwo
            every {
                importPipeline.importEnvelopeInTx(
                    txCtx,
                    envelopeOne
                )
            } returns ImportPipelineResult(outcome = ImportStatus.CREATED, providerExternalId = "item-1", restroomId = restroomIdOne)
            every {
                importPipeline.importEnvelopeInTx(
                    txCtx,
                    envelopeTwo
                )
            } returns ImportPipelineResult(outcome = ImportStatus.CREATED, providerExternalId = "item-2", restroomId = restroomIdTwo)
            every { importInboxRepository.markSuccessInTx(txCtx, importIdOne, null, restroomIdOne) } just Runs
            every { importInboxRepository.markSuccessInTx(txCtx, importIdTwo, null, restroomIdTwo) } just Runs

            val summary =
                processor.process(
                    provider = ImportProvider.TWO_GIS,
                    payloadType = ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON,
                    cityId = null,
                    items = listOf(payloadOne, payloadTwo),
                    chunkSize = 10
                )

            assertEquals(2, summary.successful)
            coVerify(exactly = 1) {
                importCityResolver.resolveByMetadata(
                    countryName = "Беларусь",
                    cityName = "Минск"
                )
            }
        }

    private fun stubTransactionRunner() {
        mockkStatic("yayauheny.by.util.TransactionExtensionsKt")
        every { txCtx.connectionResult<Any?>(any()) } answers {
            val callable = arg<ConnectionCallable<Any?>>(0)
            callable.run(connection)
        }
        coEvery { ctx.transactionSuspend<Unit>(any()) } coAnswers {
            val block = arg<suspend (DSLContext) -> Unit>(1)
            block(txCtx)
        }
    }

    private fun sampleEnvelope(
        providerObjectId: String = "item-1",
        payloadHash: String = "hash-item-1",
        cityId: UUID = UUID.randomUUID()
    ): ProviderImportEnvelope =
        ProviderImportEnvelope(
            inboxMetadata =
                InboxMetadata(
                    provider = ImportProvider.TWO_GIS,
                    entityType = ImportEntityType.PLACE,
                    externalId = providerObjectId,
                    sourceUrl = null,
                    scrapedAt = null,
                    payloadHash = payloadHash
                ),
            command =
                NormalizedRestroomCandidate(
                    provider = ImportProvider.TWO_GIS,
                    providerObjectId = providerObjectId,
                    cityId = cityId,
                    name = "Test Toilet",
                    address = "Test Street 1",
                    lat = 53.9,
                    lng = 27.5,
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
