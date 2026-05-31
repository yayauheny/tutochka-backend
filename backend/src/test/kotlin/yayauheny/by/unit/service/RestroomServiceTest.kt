package yayauheny.by.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.data.domain.PageImpl
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.BuildingEntity
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.entity.SubwayStationEntity
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.restroom.RestroomFilter
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository
import yayauheny.by.service.RestroomService
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.dto.Coordinates
import java.util.Optional
import java.util.UUID

@DisplayName("RestroomService Tests")
class RestroomServiceTest {
    private val restroomRepository = mockk<RestroomJpaRepository>()
    private val cityRepository = mockk<CityJpaRepository>()
    private val buildingRepository = mockk<BuildingJpaRepository>()
    private val subwayStationRepository = mockk<SubwayStationJpaRepository>()
    private val restroomService =
        RestroomService(
            restroomRepository = restroomRepository,
            cityRepository = cityRepository,
            buildingRepository = buildingRepository,
            subwayStationRepository = subwayStationRepository
        )

    private val geometryFactory = GeometryFactory()

    // Read behavior tests

    @Test
    fun `getAllRestrooms uses supplied filter`() {
        val filter = RestroomFilter(status = RestroomStatus.ACTIVE)
        val pagination = PaginationRequest(page = 0, size = 10)
        val restroom = restroomEntity()
        every { restroomRepository.findAllByFilter(filter, pagination) } returns PageImpl(listOf(restroom))

        val result = restroomService.getAllRestrooms(filter, pagination)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().name).isEqualTo(restroom.name)
        verify { restroomRepository.findAllByFilter(filter, pagination) }
    }

    @Test
    fun `getRestroomById returns null when absent`() {
        val id = UUID.randomUUID()
        every { restroomRepository.findById(id) } returns Optional.empty()

        val result = restroomService.getRestroomById(id)

        assertThat(result).isNull()
    }

    @Test
    fun `getRestroomById maps DTO fields`() {
        val restroom = restroomEntity(name = "Central", address = "Main st")
        every { restroomRepository.findById(restroom.id!!) } returns Optional.of(restroom)

        val result = restroomService.getRestroomById(restroom.id!!)

        assertThat(result?.id).isEqualTo(restroom.id)
        assertThat(result?.name).isEqualTo("Central")
        assertThat(result?.address).isEqualTo("Main st")
        assertThat(result?.coordinates).isEqualTo(Coordinates(lat = 53.9, lon = 27.56))
    }

    @Test
    fun `getRestroomsByCity uses city filter`() {
        val cityId = UUID.randomUUID()
        val pagination = PaginationRequest(page = 0, size = 10)
        val restroom = restroomEntity(cityId = cityId)
        every { restroomRepository.findAllByFilter(any(), pagination) } returns PageImpl(listOf(restroom))

        val result = restroomService.getRestroomsByCity(cityId, pagination)

        assertThat(result.content).hasSize(1)
        verify { restroomRepository.findAllByFilter(match { it.cityId == cityId }, pagination) }
    }

    @Test
    fun `findNearestRestrooms sets display name priority name, address, Unknown`() {
        val lat = 53.9
        val lon = 27.56
        
        val withName = restroomEntity().apply { name = "Name"; address = "Address" }
        val withAddressOnly = restroomEntity().apply { name = ""; address = "Address Only" }
        val unknown = restroomEntity().apply { name = null; address = null }
        
        every { restroomRepository.findNearest(lon, lat, 5000, 3) } returns listOf(withName, withAddressOnly, unknown)

        val result = restroomService.findNearestRestrooms(lat, lon, 3)

        assertThat(result[0].displayName).isEqualTo("Name")
        assertThat(result[1].displayName).isEqualTo("Address Only")
        assertThat(result[2].displayName).isEqualTo("Unknown")
    }

    @Test
    fun `findNearestRestrooms sets queryCoordinates and computes service-side distance`() {
        val lat = 53.9
        val lon = 27.56
        val restroom = restroomEntity(lat = 53.9005, lon = 27.5605)
        every { restroomRepository.findNearest(lon, lat, 5000, 1) } returns listOf(restroom)

        val result = restroomService.findNearestRestrooms(lat, lon, 1)

        val dto = result.single()
        assertThat(dto.queryCoordinates).isEqualTo(Coordinates(lat = lat, lon = lon))
        assertThat(dto.restroomCoordinates).isEqualTo(Coordinates(lat = 53.9005, lon = 27.5605))
        assertThat(dto.distanceMeters).isGreaterThan(0.0)
    }

    // Create behavior tests

    @Test
    fun `createRestroom resolves city, building, and subwayStation`() {
        val city = cityEntity()
        val building = buildingEntity()
        val station = subwayStationEntity()
        val createDto = TestDataHelpers.createRestroomCreateDto(
            cityId = city.id!!,
            buildingId = building.id!!,
            subwayStationId = station.id!!
        )

        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        every { buildingRepository.findById(building.id!!) } returns Optional.of(building)
        every { subwayStationRepository.findById(station.id!!) } returns Optional.of(station)
        
        val entitySlot = slot<RestroomEntity>()
        every { restroomRepository.save(capture(entitySlot)) } answers { 
            firstArg<RestroomEntity>().apply { 
                id = UUID.randomUUID()
                createdAt = java.time.Instant.now()
                updatedAt = java.time.Instant.now()
            }
        }

        restroomService.createRestroom(createDto)

        val saved = entitySlot.captured
        assertThat(saved.city).isEqualTo(city)
        assertThat(saved.building).isEqualTo(building)
        assertThat(saved.subwayStation).isEqualTo(station)
    }

    @Test
    fun `createRestroom throws when relations are missing`() {
        val createDto = TestDataHelpers.createRestroomCreateDto(
            cityId = UUID.randomUUID(),
            buildingId = UUID.randomUUID(),
            subwayStationId = UUID.randomUUID()
        )

        every { cityRepository.findById(any()) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.createRestroom(createDto) }

        every { cityRepository.findById(any()) } returns Optional.of(cityEntity())
        every { buildingRepository.findById(any()) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.createRestroom(createDto) }

        every { buildingRepository.findById(any()) } returns Optional.of(buildingEntity())
        every { subwayStationRepository.findById(any()) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.createRestroom(createDto) }
    }

    @Test
    fun `createRestroom converts collections and booleans correctly`() {
        val city = cityEntity()
        val createDto = TestDataHelpers.createRestroomCreateDto(cityId = city.id!!).copy(
            phones = mapOf("primary" to "+123", "empty" to ""),
            amenities = mapOf("wheelchair" to true, "soap" to "true", "music" to "false", "weird" to "invalid"),
            externalMaps = mapOf("yandex" to "http://yandex")
        )

        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        val entitySlot = slot<RestroomEntity>()
        every { restroomRepository.save(capture(entitySlot)) } answers { 
            firstArg<RestroomEntity>().apply { id = UUID.randomUUID(); createdAt = java.time.Instant.now(); updatedAt = java.time.Instant.now() }
        }

        restroomService.createRestroom(createDto)

        val saved = entitySlot.captured
        assertThat(saved.phones).containsExactly("+123")
        assertThat(saved.amenities).containsEntry("wheelchair", true)
        assertThat(saved.amenities).containsEntry("soap", true)
        assertThat(saved.amenities).containsEntry("music", false)
        assertThat(saved.amenities).containsEntry("weird", false)
        assertThat(saved.externalMaps).containsEntry("yandex", "http://yandex")
        assertThat(saved.hasPhotos).isTrue()
        assertThat(saved.inheritBuildingSchedule).isFalse()
    }

    // Update behavior tests

    @Test
    fun `updateRestroom throws when entity or relations are missing`() {
        val id = UUID.randomUUID()
        val updateDto = TestDataHelpers.createRestroomUpdateDto(cityId = UUID.randomUUID(), buildingId = UUID.randomUUID(), subwayStationId = UUID.randomUUID())

        every { restroomRepository.findById(id) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.updateRestroom(id, updateDto) }

        val entity = restroomEntity()
        every { restroomRepository.findById(id) } returns Optional.of(entity)
        every { cityRepository.findById(any()) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.updateRestroom(id, updateDto) }

        every { cityRepository.findById(any()) } returns Optional.of(cityEntity())
        every { buildingRepository.findById(any()) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.updateRestroom(id, updateDto) }

        every { buildingRepository.findById(any()) } returns Optional.of(buildingEntity())
        every { subwayStationRepository.findById(any()) } returns Optional.empty()
        assertThrows<IllegalArgumentException> { restroomService.updateRestroom(id, updateDto) }
    }

    @Test
    fun `updateRestroom updates fields correctly and preserves missing optional fields`() {
        val id = UUID.randomUUID()
        val originalName = "Old Name"
        val entity = restroomEntity().apply { name = originalName }
        val updateDto = TestDataHelpers.createRestroomUpdateDto(
            name = null, // Should preserve original
            status = RestroomStatus.INACTIVE, // Direct assign
            hasPhotos = false // Direct assign
        ).copy(
            cityId = null, buildingId = null, subwayStationId = null,
            phones = mapOf("primary" to "+999"),
            amenities = mapOf("soap" to true)
        )

        every { restroomRepository.findById(id) } returns Optional.of(entity)
        val entitySlot = slot<RestroomEntity>()
        every { restroomRepository.save(capture(entitySlot)) } answers { firstArg() }

        restroomService.updateRestroom(id, updateDto)

        val saved = entitySlot.captured
        assertThat(saved.name).isEqualTo(originalName) // Preserved
        assertThat(saved.status).isEqualTo(RestroomStatus.INACTIVE) // Overwritten
        assertThat(saved.hasPhotos).isFalse() // Overwritten
        assertThat(saved.phones).containsExactly("+999") // Overwritten
        assertThat(saved.amenities).containsEntry("soap", true) // Overwritten
    }

    @Test
    fun `updateRestroom rewrites resolved relations and direct assignment fields`() {
        val id = UUID.randomUUID()
        val city = cityEntity()
        val building = buildingEntity()
        val station = subwayStationEntity()
        val entity = restroomEntity().apply {
            status = RestroomStatus.ACTIVE
            hasPhotos = true
            inheritBuildingSchedule = false
        }
        val updateDto = TestDataHelpers.createRestroomUpdateDto(
            cityId = city.id!!,
            buildingId = building.id!!,
            subwayStationId = station.id!!,
            name = "Updated Name",
            address = "Updated address",
            status = RestroomStatus.INACTIVE,
            inheritBuildingSchedule = true,
            hasPhotos = false
        ).copy(
            locationType = yayauheny.by.model.enums.LocationType.STANDALONE,
            originProvider = yayauheny.by.model.enums.ImportProvider.TWO_GIS,
            originId = "origin-1",
            isHidden = true,
            coordinates = Coordinates(lat = 10.0, lon = 20.0)
        )

        every { restroomRepository.findById(id) } returns Optional.of(entity)
        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        every { buildingRepository.findById(building.id!!) } returns Optional.of(building)
        every { subwayStationRepository.findById(station.id!!) } returns Optional.of(station)
        val entitySlot = slot<RestroomEntity>()
        every { restroomRepository.save(capture(entitySlot)) } answers { firstArg() }

        restroomService.updateRestroom(id, updateDto)

        val saved = entitySlot.captured
        assertThat(saved.city).isEqualTo(city)
        assertThat(saved.building).isEqualTo(building)
        assertThat(saved.subwayStation).isEqualTo(station)
        assertThat(saved.name).isEqualTo("Updated Name")
        assertThat(saved.address).isEqualTo("Updated address")
        assertThat(saved.status).isEqualTo(RestroomStatus.INACTIVE)
        assertThat(saved.inheritBuildingSchedule).isTrue()
        assertThat(saved.hasPhotos).isFalse()
        assertThat(saved.locationType).isEqualTo(yayauheny.by.model.enums.LocationType.STANDALONE)
        assertThat(saved.originProvider).isEqualTo(yayauheny.by.model.enums.ImportProvider.TWO_GIS)
        assertThat(saved.originId).isEqualTo("origin-1")
        assertThat(saved.isHidden).isTrue()
        assertThat(saved.coordinates?.x).isEqualTo(20.0)
        assertThat(saved.coordinates?.y).isEqualTo(10.0)
    }

    @Test
    fun `deleteRestroom returns true when entity exists`() {
        val id = UUID.randomUUID()
        every { restroomRepository.existsById(id) } returns true
        every { restroomRepository.deleteById(id) } returns Unit

        val result = restroomService.deleteRestroom(id)

        assertThat(result).isTrue()
        verify { restroomRepository.deleteById(id) }
    }

    // Delete behavior tests

    @Test
    fun `deleteRestroom returns false when missing`() {
        val id = UUID.randomUUID()
        every { restroomRepository.existsById(id) } returns false

        val result = restroomService.deleteRestroom(id)

        assertThat(result).isFalse()
        verify(exactly = 0) { restroomRepository.deleteById(any()) }
    }

    @Test
    fun `boolean map conversion handles actual types`() {
        val city = cityEntity()
        val createDto = TestDataHelpers.createRestroomCreateDto(cityId = city.id!!).copy(
            amenities = mapOf(
                "k1" to true,
                "k2" to "true",
                "k3" to false,
                "k4" to "false",
                "k5" to "invalid",
                "k6" to 123
            )
        )

        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        val entitySlot = slot<RestroomEntity>()
        every { restroomRepository.save(capture(entitySlot)) } answers { 
            firstArg<RestroomEntity>().apply { id = UUID.randomUUID(); createdAt = java.time.Instant.now(); updatedAt = java.time.Instant.now() }
        }

        restroomService.createRestroom(createDto)

        val saved = entitySlot.captured
        assertThat(saved.amenities!!["k1"]).isEqualTo(true)
        assertThat(saved.amenities!!["k2"]).isEqualTo(true)
        assertThat(saved.amenities!!["k3"]).isEqualTo(false)
        assertThat(saved.amenities!!["k4"]).isEqualTo(false)
        assertThat(saved.amenities!!["k5"]).isEqualTo(false)
        assertThat(saved.amenities!!["k6"]).isEqualTo(false)
    }

    private fun cityEntity(): CityEntity =
        CityEntity().apply {
            id = UUID.randomUUID()
            country = CountryEntity().apply { id = UUID.randomUUID(); code = "US"; nameRu = "США"; nameEn = "USA" }
            nameRu = "Минск"
            nameEn = "Minsk"
            coordinates = geometryFactory.createPoint(Coordinate(27.56, 53.9))
        }

    private fun buildingEntity(): BuildingEntity =
        BuildingEntity().apply {
            id = UUID.randomUUID()
            city = cityEntity()
            name = "Building"
            coordinates = geometryFactory.createPoint(Coordinate(27.56, 53.9))
            createdAt = java.time.Instant.now()
            updatedAt = java.time.Instant.now()
        }

    private fun subwayStationEntity(): SubwayStationEntity =
        SubwayStationEntity().apply {
            id = UUID.randomUUID()
            nameRu = "Station"
            nameEn = "Station"
            coordinates = geometryFactory.createPoint(Coordinate(27.56, 53.9))
            createdAt = java.time.Instant.now()
        }

    private fun restroomEntity(
        cityId: UUID? = UUID.randomUUID(),
        name: String? = "Restroom",
        address: String? = "Address",
        lat: Double = 53.9,
        lon: Double = 27.56
    ): RestroomEntity =
        RestroomEntity().apply {
            id = UUID.randomUUID()
            city = cityEntity().apply { id = cityId }
            this.name = name
            this.address = address
            coordinates = geometryFactory.createPoint(Coordinate(lon, lat))
            createdAt = java.time.Instant.now()
            updatedAt = java.time.Instant.now()
        }
}
