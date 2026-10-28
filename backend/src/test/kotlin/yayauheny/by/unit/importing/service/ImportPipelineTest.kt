package yayauheny.by.unit.importing.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityManager
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.entity.SubwayStationEntity
import yayauheny.by.importing.model.ImportEntityType
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.importing.service.ImportPipeline
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.imports.ImportStatus
import yayauheny.by.model.imports.NormalizedRestroomCandidate
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.util.point

@DisplayName("ImportPipeline Tests")
class ImportPipelineTest {
    private val buildingRepository = mockk<BuildingJpaRepository>()
    private val cityRepository = mockk<CityJpaRepository>()
    private val restroomRepository = mockk<RestroomJpaRepository>()
    private val entityManager = mockk<EntityManager>()
    private val jdbcTemplate = mockk<JdbcTemplate>(relaxed = true)
    private val objectMapper = jacksonObjectMapper()
    private val importPipeline =
        ImportPipeline(
            buildingRepository = buildingRepository,
            cityRepository = cityRepository,
            restroomRepository = restroomRepository,
            entityManager = entityManager,
            jdbcTemplate = jdbcTemplate,
            objectMapper = objectMapper
        )

    @Test
    fun `importEnvelope preserves manual fields when updating existing imported restroom`() {
        val cityId = UUID.randomUUID()
        val cityReference = CityEntity().apply { id = cityId }
        val existing = restroomEntity(cityId).apply {
            id = UUID.randomUUID()
            name = "Old name"
            address = "Old address"
            phones = listOf("+375291112233")
            accessNote = "keep-note"
            directionGuide = "keep-guide"
            hasPhotos = true
            isHidden = true
            moderationStatus = "PENDING"
            subwayStation = SubwayStationEntity().apply { id = UUID.randomUUID() }
            twoGisId = "old-id"
        }
        val candidate =
            NormalizedRestroomCandidate(
                provider = ImportProvider.TWO_GIS,
                providerObjectId = "new-id",
                cityId = cityId,
                name = "Updated name",
                address = "Updated address",
                lat = 53.9,
                lng = 27.57,
                placeType = PlaceType.PUBLIC,
                locationType = LocationType.STANDALONE,
                feeType = FeeType.FREE,
                accessibilityType = AccessibilityType.WHEELCHAIR,
                status = RestroomStatus.ACTIVE,
                amenities = objectMapper.createObjectNode().put("soap", true)
            )
        val envelope =
            ProviderImportEnvelope(
                inboxMetadata =
                    InboxMetadata(
                        provider = ImportProvider.TWO_GIS,
                        entityType = ImportEntityType.PLACE,
                        externalId = "new-id",
                        sourceUrl = null,
                        scrapedAt = null,
                        payloadHash = "hash"
                    ),
                command = candidate,
                rawPayload = objectMapper.createObjectNode()
            )

        every { entityManager.getReference(CityEntity::class.java, cityId) } returns cityReference
        every { restroomRepository.findByOriginProviderAndOriginId(ImportProvider.TWO_GIS, "new-id") } returns existing
        every { restroomRepository.saveAndFlush(any()) } answers { firstArg() }

        val result = importPipeline.importEnvelope(envelope)

        assertThat(result.outcome).isEqualTo(ImportStatus.UPDATED)
        assertThat(existing.name).isEqualTo("Updated name")
        assertThat(existing.address).isEqualTo("Updated address")
        assertThat(existing.coordinates?.x).isEqualTo(27.57)
        assertThat(existing.coordinates?.y).isEqualTo(53.9)
        assertThat(existing.phones).containsExactly("+375291112233")
        assertThat(existing.accessNote).isEqualTo("keep-note")
        assertThat(existing.directionGuide).isEqualTo("keep-guide")
        assertThat(existing.hasPhotos).isTrue()
        assertThat(existing.isHidden).isTrue()
        assertThat(existing.moderationStatus).isEqualTo("PENDING")
        assertThat(existing.subwayStation?.id).isNotNull()
        assertThat(existing.twoGisId).isEqualTo("old-id")
        assertThat(existing.originId).isEqualTo("new-id")
        verify(exactly = 1) { restroomRepository.saveAndFlush(existing) }
    }

    @Test
    fun `importEnvelope skips duplicate when external map already exists for same provider object id`() {
        val cityId = UUID.randomUUID()
        val existing = restroomEntity(cityId).apply {
            id = UUID.randomUUID()
            yandexId = "shared-id"
        }
        val candidate = candidate(provider = ImportProvider.YANDEX_MAPS, providerObjectId = "shared-id", cityId = cityId)
        val envelope = envelope(candidate)

        every { restroomRepository.findByOriginProviderAndOriginId(ImportProvider.YANDEX_MAPS, "shared-id") } returns null
        every { restroomRepository.findByExternalMap("yandex", "shared-id") } returns existing

        val result = importPipeline.importEnvelope(envelope)

        assertThat(result.outcome).isEqualTo(ImportStatus.SKIPPED_DUPLICATE)
        assertThat(result.duplicateReason).isEqualTo("EXACT_EXTERNAL_MAP")
        assertThat(result.duplicateOfRestroomId).isEqualTo(existing.id)
    }

    @Test
    fun `importEnvelope links duplicate when restroom match key already exists`() {
        val cityId = UUID.randomUUID()
        val existing = restroomEntity(cityId).apply {
            id = UUID.randomUUID()
            twoGisId = null
            yandexId = null
            googleId = null
            appleId = null
        }
        val candidate = candidate(provider = ImportProvider.TWO_GIS, providerObjectId = "new-id", cityId = cityId)
        val envelope = envelope(candidate)

        every { restroomRepository.findByOriginProviderAndOriginId(ImportProvider.TWO_GIS, "new-id") } returns null
        every { restroomRepository.findByExternalMap("2gis", "new-id") } returns null
        every { restroomRepository.findByRestroomMatchKey(any()) } returns existing
        every { restroomRepository.saveAndFlush(existing) } returns existing

        val result = importPipeline.importEnvelope(envelope)

        assertThat(result.outcome).isEqualTo(ImportStatus.LINKED_DUPLICATE)
        assertThat(result.duplicateReason).isEqualTo("EXACT_RESTROOM_MATCH_KEY")
        assertThat(result.duplicateOfRestroomId).isEqualTo(existing.id)
        assertThat(existing.twoGisId).isEqualTo("new-id")
        verify(exactly = 1) { restroomRepository.saveAndFlush(existing) }
    }

    private fun restroomEntity(cityId: UUID): RestroomEntity =
        RestroomEntity().apply {
            city = CityEntity().apply { id = cityId }
            coordinates = point(27.56, 53.9)
            placeType = PlaceType.OTHER
            feeType = FeeType.UNKNOWN
            accessibilityType = AccessibilityType.UNKNOWN
            status = RestroomStatus.ACTIVE
            locationType = LocationType.UNKNOWN
            originProvider = ImportProvider.TWO_GIS
            originId = "old-id"
            twoGisId = "old-id"
        }

    private fun candidate(
        provider: ImportProvider,
        providerObjectId: String,
        cityId: UUID
    ): NormalizedRestroomCandidate =
        NormalizedRestroomCandidate(
            provider = provider,
            providerObjectId = providerObjectId,
            cityId = cityId,
            name = "Туалет",
            address = "Минск, ул. Киселёва, 3",
            lat = 53.909435,
            lng = 27.573736,
            placeType = PlaceType.PUBLIC,
            locationType = LocationType.STANDALONE,
            feeType = FeeType.FREE,
            accessibilityType = AccessibilityType.UNKNOWN,
            status = RestroomStatus.ACTIVE,
            amenities = objectMapper.createObjectNode()
        )

    private fun envelope(candidate: NormalizedRestroomCandidate): ProviderImportEnvelope =
        ProviderImportEnvelope(
            inboxMetadata =
                InboxMetadata(
                    provider = candidate.provider,
                    entityType = ImportEntityType.PLACE,
                    externalId = candidate.providerObjectId,
                    sourceUrl = null,
                    scrapedAt = null,
                    payloadHash = "hash"
                ),
            command = candidate,
            rawPayload = objectMapper.createObjectNode()
        )
}
