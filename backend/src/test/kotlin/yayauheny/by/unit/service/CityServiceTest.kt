package yayauheny.by.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.data.domain.PageImpl
import yayauheny.by.common.errors.ConflictException
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
    private val cityRepository = mockk<CityJpaRepository>()
    private val countryRepository = mockk<CountryJpaRepository>()
    private val cityService = CityService(cityRepository, countryRepository)
    private val geometryFactory = GeometryFactory()

    // Read behavior tests

    @Test
    fun `getAllCities maps entities to response dto`() {
        val pagination = PaginationRequest(page = 0, size = 10)
        val city = cityEntity()
        every { cityRepository.findAllByFilter(any(), pagination) } returns PageImpl(listOf(city))

        val result = cityService.getAllCities(pagination)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().nameRu).isEqualTo(city.nameRu)
        verify { cityRepository.findAllByFilter(CityFilter(), pagination) }
    }

    @Test
    fun `getCitiesByCountry delegates through filter and returns mapped DTOs`() {
        val countryId = UUID.randomUUID()
        val pagination = PaginationRequest(page = 0, size = 10)
        val city = cityEntity()
        every { cityRepository.findAllByFilter(any(), pagination) } returns PageImpl(listOf(city))

        val result = cityService.getCitiesByCountry(countryId, pagination)

        assertThat(result.content).hasSize(1)
        verify { cityRepository.findAllByFilter(CityFilter(countryId = countryId), pagination) }
    }

    @Test
    fun `searchCitiesByName delegates through filter and returns mapped DTOs`() {
        val name = "Minsk"
        val pagination = PaginationRequest(page = 0, size = 10)
        val city = cityEntity()
        every { cityRepository.findAllByFilter(any(), pagination) } returns PageImpl(listOf(city))

        val result = cityService.searchCitiesByName(name, pagination)

        assertThat(result.content).hasSize(1)
        verify { cityRepository.findAllByFilter(CityFilter(name = name), pagination) }
    }

    @Test
    fun `getCityById returns mapped dto`() {
        val city = cityEntity()
        every { cityRepository.findById(city.id!!) } returns Optional.of(city)

        val result = cityService.getCityById(city.id!!)

        assertThat(result?.nameEn).isEqualTo(city.nameEn)
        verify { cityRepository.findById(city.id!!) }
    }

    @Test
    fun `getCityById returns null when absent`() {
        val id = UUID.randomUUID()
        every { cityRepository.findById(id) } returns Optional.empty()

        val result = cityService.getCityById(id)

        assertThat(result).isNull()
    }

    // Create behavior tests

    @Test
    fun `createCity saves new entity with mapped fields and correct coordinates`() {
        val country = countryEntity()
        val createDto = TestDataHelpers.createCityCreateDto(countryId = country.id!!, lat = 53.9, lon = 27.56)
        every { countryRepository.findById(country.id!!) } returns Optional.of(country)
        every { cityRepository.findByCountryIdAndNameRu(country.id!!, createDto.nameRu) } returns null
        every { cityRepository.findByCountryIdAndNameEn(country.id!!, createDto.nameEn) } returns null
        
        val citySlot = slot<CityEntity>()
        every { cityRepository.save(capture(citySlot)) } answers {
            firstArg<CityEntity>().apply { id = UUID.randomUUID() }
        }

        val result = cityService.createCity(createDto)

        val saved = citySlot.captured
        assertThat(saved.country).isEqualTo(country)
        assertThat(saved.nameRu).isEqualTo(createDto.nameRu)
        assertThat(saved.nameEn).isEqualTo(createDto.nameEn)
        assertThat(saved.region).isEqualTo(createDto.region)
        assertThat(saved.coordinates?.x).isEqualTo(27.56)
        assertThat(saved.coordinates?.y).isEqualTo(53.9)
        assertThat(result.countryId).isEqualTo(country.id)
        assertThat(result.nameRu).isEqualTo(createDto.nameRu)
        assertThat(result.nameEn).isEqualTo(createDto.nameEn)
        assertThat(result.region).isEqualTo(createDto.region)
        assertThat(result.coordinates.lat).isEqualTo(53.9)
        assertThat(result.coordinates.lon).isEqualTo(27.56)
    }

    @Test
    fun `createCity throws when country is missing`() {
        val createDto = TestDataHelpers.createCityCreateDto(countryId = UUID.randomUUID())
        every { countryRepository.findById(createDto.countryId) } returns Optional.empty()

        assertThrows<IllegalArgumentException> { cityService.createCity(createDto) }
    }

    @Test
    fun `createCity throws ConflictException when Russian name already exists in country`() {
        val country = countryEntity()
        val createDto = TestDataHelpers.createCityCreateDto(countryId = country.id!!)
        every { countryRepository.findById(country.id!!) } returns Optional.of(country)
        every { cityRepository.findByCountryIdAndNameRu(country.id!!, createDto.nameRu) } returns cityEntity()

        assertThrows<ConflictException> { cityService.createCity(createDto) }
    }

    @Test
    fun `createCity throws ConflictException when English name already exists in country`() {
        val country = countryEntity()
        val createDto = TestDataHelpers.createCityCreateDto(countryId = country.id!!)
        every { countryRepository.findById(country.id!!) } returns Optional.of(country)
        every { cityRepository.findByCountryIdAndNameRu(country.id!!, createDto.nameRu) } returns null
        every { cityRepository.findByCountryIdAndNameEn(country.id!!, createDto.nameEn) } returns cityEntity()

        assertThrows<ConflictException> { cityService.createCity(createDto) }
    }

    // Update behavior tests

    @Test
    fun `updateCity rewrites country, names, region, and coordinates correctly`() {
        val id = UUID.randomUUID()
        val country = countryEntity()
        val newCountry = countryEntity().apply { this.id = UUID.randomUUID() }
        val city = cityEntity().apply { this.id = id; this.country = country }
        val updateDto = TestDataHelpers.createCityUpdateDto(
            countryId = newCountry.id!!,
            nameRu = "UpdatedRu",
            nameEn = "UpdatedEn",
            region = "UpdatedRegion"
        ).copy(coordinates = yayauheny.by.model.dto.Coordinates(lat = 1.0, lon = 2.0))

        every { cityRepository.findById(id) } returns Optional.of(city)
        every { countryRepository.findById(newCountry.id!!) } returns Optional.of(newCountry)
        every { cityRepository.findByCountryIdAndNameRu(newCountry.id!!, "UpdatedRu") } returns null
        every { cityRepository.findByCountryIdAndNameEn(newCountry.id!!, "UpdatedEn") } returns null
        
        val citySlot = slot<CityEntity>()
        every { cityRepository.save(capture(citySlot)) } answers { firstArg() }

        cityService.updateCity(id, updateDto)

        val saved = citySlot.captured
        assertThat(saved.country).isEqualTo(newCountry)
        assertThat(saved.nameRu).isEqualTo("UpdatedRu")
        assertThat(saved.nameEn).isEqualTo("UpdatedEn")
        assertThat(saved.region).isEqualTo("UpdatedRegion")
        assertThat(saved.coordinates?.x).isEqualTo(2.0)
        assertThat(saved.coordinates?.y).isEqualTo(1.0)
    }

    @Test
    fun `updateCity throws when city is missing`() {
        val id = UUID.randomUUID()
        val updateDto = TestDataHelpers.createCityUpdateDto()
        every { cityRepository.findById(id) } returns Optional.empty()

        assertThrows<IllegalArgumentException> { cityService.updateCity(id, updateDto) }
    }

    @Test
    fun `updateCity throws when update country is missing`() {
        val id = UUID.randomUUID()
        val updateDto = TestDataHelpers.createCityUpdateDto(countryId = UUID.randomUUID())
        every { cityRepository.findById(id) } returns Optional.of(cityEntity())
        every { countryRepository.findById(updateDto.countryId!!) } returns Optional.empty()

        assertThrows<IllegalArgumentException> { cityService.updateCity(id, updateDto) }
    }

    @Test
    fun `updateCity throws ConflictException when Russian name already exists in target country`() {
        val id = UUID.randomUUID()
        val targetCountry = countryEntity().apply { this.id = UUID.randomUUID() }
        val city = cityEntity().apply { this.id = id }
        val duplicate = cityEntity().apply { this.id = UUID.randomUUID(); this.country = targetCountry }
        val updateDto = TestDataHelpers.createCityUpdateDto(
            countryId = targetCountry.id!!,
            nameRu = "Минск",
            nameEn = "Minsk Updated"
        )

        every { cityRepository.findById(id) } returns Optional.of(city)
        every { countryRepository.findById(targetCountry.id!!) } returns Optional.of(targetCountry)
        every { cityRepository.findByCountryIdAndNameRu(targetCountry.id!!, updateDto.nameRu) } returns duplicate

        assertThrows<ConflictException> { cityService.updateCity(id, updateDto) }
        verify(exactly = 0) { cityRepository.save(any()) }
    }

    @Test
    fun `updateCity throws ConflictException when English name already exists in target country`() {
        val id = UUID.randomUUID()
        val targetCountry = countryEntity().apply { this.id = UUID.randomUUID() }
        val city = cityEntity().apply { this.id = id }
        val duplicate = cityEntity().apply { this.id = UUID.randomUUID(); this.country = targetCountry }
        val updateDto = TestDataHelpers.createCityUpdateDto(
            countryId = targetCountry.id!!,
            nameRu = "Минск Updated",
            nameEn = "Minsk"
        )

        every { cityRepository.findById(id) } returns Optional.of(city)
        every { countryRepository.findById(targetCountry.id!!) } returns Optional.of(targetCountry)
        every { cityRepository.findByCountryIdAndNameRu(targetCountry.id!!, updateDto.nameRu) } returns null
        every { cityRepository.findByCountryIdAndNameEn(targetCountry.id!!, updateDto.nameEn) } returns duplicate

        assertThrows<ConflictException> { cityService.updateCity(id, updateDto) }
        verify(exactly = 0) { cityRepository.save(any()) }
    }

    // Delete behavior tests

    @Test
    fun `deleteCity returns true when entity exists`() {
        val id = UUID.randomUUID()
        every { cityRepository.existsById(id) } returns true
        every { cityRepository.deleteById(id) } returns Unit

        val result = cityService.deleteCity(id)

        assertThat(result).isTrue()
        verify { cityRepository.deleteById(id) }
    }

    @Test
    fun `deleteCity returns false when missing`() {
        val id = UUID.randomUUID()
        every { cityRepository.existsById(id) } returns false

        val result = cityService.deleteCity(id)

        assertThat(result).isFalse()
        verify(exactly = 0) { cityRepository.deleteById(any()) }
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
