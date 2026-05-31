package yayauheny.by.unit.service

import io.mockk.every
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository
import yayauheny.by.service.RestroomService

@DisplayName("RestroomService Tests")
class RestroomServiceTest {
    private val restroomRepository = io.mockk.mockk<RestroomJpaRepository>()
    private val cityRepository = io.mockk.mockk<CityJpaRepository>()
    private val buildingRepository = io.mockk.mockk<BuildingJpaRepository>()
    private val subwayStationRepository = io.mockk.mockk<SubwayStationJpaRepository>()
    private val restroomService =
        RestroomService(
            restroomRepository = restroomRepository,
            cityRepository = cityRepository,
            buildingRepository = buildingRepository,
            subwayStationRepository = subwayStationRepository
        )

    private val geometryFactory = GeometryFactory()

    @Test
    fun get_all_restrooms_maps_entities_to_response_dto() {
        val pagination = PaginationRequest(page = 0, size = 10)
        val restroom = restroomEntity()
        every { restroomRepository.findAllByFilter(any(), pagination) } returns pageOf(restroom)

        val result = restroomService.getAllRestrooms(pagination)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().name).isEqualTo(restroom.name)
        verify { restroomRepository.findAllByFilter(any(), pagination) }
    }

    @Test
    fun get_restroom_by_id_returns_mapped_dto() {
        val restroom = restroomEntity()
        every { restroomRepository.findById(restroom.id!!) } returns Optional.of(restroom)

        val result = restroomService.getRestroomById(restroom.id!!)

        assertThat(result?.name).isEqualTo(restroom.name)
        verify { restroomRepository.findById(restroom.id!!) }
    }

    @Test
    fun get_restrooms_by_city_uses_city_filter() {
        val cityId = UUID.randomUUID()
        val pagination = PaginationRequest(page = 0, size = 10)
        val restroom = restroomEntity(cityId = cityId)
        every { restroomRepository.findAllByFilter(any(), pagination) } returns pageOf(restroom)

        val result = restroomService.getRestroomsByCity(cityId, pagination)

        assertThat(result.content).hasSize(1)
        verify { restroomRepository.findAllByFilter(match { it.cityId == cityId }, pagination) }
    }

    @Test
    fun find_nearest_restrooms_returns_distance_calculated_from_coordinates() {
        val latitude = 53.9
        val longitude = 27.56
        val restroom = restroomEntity(lat = 53.9005, lon = 27.5605)
        every { restroomRepository.findNearest(longitude, latitude, 1000, 5) } returns listOf(restroom)

        val result = restroomService.findNearestRestrooms(latitude, longitude, 5, 1000)

        assertThat(result).hasSize(1)
        assertThat(result.single().queryCoordinates).isEqualTo(Coordinates(lat = latitude, lon = longitude))
        assertThat(result.single().distanceMeters).isGreaterThan(0.0)
        verify { restroomRepository.findNearest(longitude, latitude, 1000, 5) }
    }

    @Test
    fun create_restroom_resolves_city_and_saves_entity() {
        val city = cityEntity()
        val createDto = TestDataHelpers.createRestroomCreateDto(cityId = city.id!!)
        val saved = restroomEntity(cityId = city.id!!, name = createDto.name, address = createDto.address)
        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        every { restroomRepository.save(any()) } answers {
            firstArg<RestroomEntity>().apply {
                id = UUID.randomUUID()
                createdAt = java.time.Instant.parse("2026-05-31T00:00:00Z")
                updatedAt = java.time.Instant.parse("2026-05-31T00:00:00Z")
            }
        }

        val result = restroomService.createRestroom(createDto)

        assertThat(result.name).isEqualTo(createDto.name)
        assertThat(result.status).isEqualTo(RestroomStatus.ACTIVE)
        verify { cityRepository.findById(city.id!!) }
        verify { restroomRepository.save(any()) }
    }

    @Test
    fun update_restroom_updates_entity_and_saves_it() {
        val city = cityEntity()
        val restroom = restroomEntity(cityId = city.id!!)
        val updateDto = TestDataHelpers.createRestroomUpdateDto(cityId = city.id!!, name = "Updated", address = "Updated address")
        every { restroomRepository.findById(restroom.id!!) } returns Optional.of(restroom)
        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        every { restroomRepository.save(any()) } answers { firstArg() }

        val result = restroomService.updateRestroom(restroom.id!!, updateDto)

        assertThat(result.name).isEqualTo("Updated")
        assertThat(result.address).isEqualTo("Updated address")
        verify { restroomRepository.save(restroom) }
    }

    @Test
    fun delete_restroom_returns_true_when_entity_exists() {
        val id = UUID.randomUUID()
        every { restroomRepository.existsById(id) } returns true
        every { restroomRepository.deleteById(id) } returns Unit

        val result = restroomService.deleteRestroom(id)

        assertThat(result).isTrue()
        verify { restroomRepository.deleteById(id) }
    }

    private fun cityEntity(): CityEntity =
        CityEntity().apply {
            id = UUID.randomUUID()
            country = CountryEntity().apply { id = UUID.randomUUID(); code = "US"; nameRu = "США"; nameEn = "USA" }
            nameRu = "Минск"
            nameEn = "Minsk"
            coordinates = geometryFactory.createPoint(Coordinate(27.56, 53.9))
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
            createdAt = java.time.Instant.parse("2026-05-31T00:00:00Z")
            updatedAt = java.time.Instant.parse("2026-05-31T00:00:00Z")
        }

    private fun pageOf(restroom: RestroomEntity) =
        org.springframework.data.domain.PageImpl(listOf(restroom))
}
