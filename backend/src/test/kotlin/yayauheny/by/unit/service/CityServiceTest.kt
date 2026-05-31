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
import org.springframework.data.domain.PageImpl
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.city.CityFilter
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.service.CityService

@DisplayName("CityService Tests")
class CityServiceTest {
    private val cityRepository = io.mockk.mockk<CityJpaRepository>()
    private val countryRepository = io.mockk.mockk<CountryJpaRepository>()
    private val cityService = CityService(cityRepository, countryRepository)
    private val geometryFactory = GeometryFactory()

    @Test
    fun get_all_cities_maps_entities_to_response_dto() {
        val pagination = PaginationRequest(page = 0, size = 10)
        val city = cityEntity()
        every { cityRepository.findAllByFilter(any(), pagination) } returns PageImpl(listOf(city))

        val result = cityService.getAllCities(pagination)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().nameRu).isEqualTo(city.nameRu)
        verify { cityRepository.findAllByFilter(CityFilter(), pagination) }
    }

    @Test
    fun get_city_by_id_returns_mapped_dto() {
        val city = cityEntity()
        every { cityRepository.findById(city.id!!) } returns Optional.of(city)

        val result = cityService.getCityById(city.id!!)

        assertThat(result?.nameEn).isEqualTo(city.nameEn)
        verify { cityRepository.findById(city.id!!) }
    }

    @Test
    fun create_city_saves_new_entity() {
        val country = countryEntity()
        val createDto = TestDataHelpers.createCityCreateDto(countryId = country.id!!)
        every { countryRepository.findById(country.id!!) } returns Optional.of(country)
        every { cityRepository.findByCountryIdAndNameRu(country.id!!, createDto.nameRu) } returns null
        every { cityRepository.findByCountryIdAndNameEn(country.id!!, createDto.nameEn) } returns null
        every { cityRepository.save(any()) } answers {
            firstArg<CityEntity>().apply {
                id = UUID.randomUUID()
            }
        }

        val result = cityService.createCity(createDto)

        assertThat(result.nameRu).isEqualTo(createDto.nameRu)
        verify { cityRepository.save(any()) }
    }

    @Test
    fun update_city_saves_modified_entity() {
        val country = countryEntity()
        val city = cityEntity()
        val updateDto = TestDataHelpers.createCityUpdateDto(countryId = country.id!!)
        every { cityRepository.findById(city.id!!) } returns Optional.of(city)
        every { countryRepository.findById(country.id!!) } returns Optional.of(country)
        every { cityRepository.save(any()) } answers { firstArg() }

        val result = cityService.updateCity(city.id!!, updateDto)

        assertThat(result.countryId).isEqualTo(country.id)
        verify { cityRepository.save(city) }
    }

    @Test
    fun delete_city_returns_true_when_entity_exists() {
        val id = UUID.randomUUID()
        every { cityRepository.existsById(id) } returns true
        every { cityRepository.deleteById(id) } returns Unit

        val result = cityService.deleteCity(id)

        assertThat(result).isTrue()
        verify { cityRepository.deleteById(id) }
    }

    private fun countryEntity(): CountryEntity =
        CountryEntity().apply {
            id = UUID.randomUUID()
            code = "US"
            nameRu = "США"
            nameEn = "USA"
        }

    private fun cityEntity(): CityEntity =
        CityEntity().apply {
            id = UUID.randomUUID()
            country = countryEntity()
            nameRu = "Минск"
            nameEn = "Minsk"
            region = "Минская область"
            coordinates = geometryFactory.createPoint(Coordinate(27.56, 53.9))
        }
}
