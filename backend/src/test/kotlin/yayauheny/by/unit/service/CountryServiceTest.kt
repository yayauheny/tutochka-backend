package yayauheny.by.unit.service

import io.mockk.every
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.CountryEntity
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.country.CountryFilter
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.service.CountryService

@DisplayName("CountryService Tests")
class CountryServiceTest {
    private val countryRepository = io.mockk.mockk<CountryJpaRepository>()
    private val countryService = CountryService(countryRepository)

    @Test
    fun get_all_countries_maps_entities_to_response_dto() {
        val pagination = PaginationRequest(page = 0, size = 10)
        val entity = countryEntity()
        every { countryRepository.findAllByFilter(any(), pagination) } returns pageOf(entity)

        val result = countryService.getAllCountries(pagination)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().code).isEqualTo(entity.code)
        verify { countryRepository.findAllByFilter(CountryFilter(), pagination) }
    }

    @Test
    fun get_country_by_id_returns_mapped_dto() {
        val entity = countryEntity()
        every { countryRepository.findById(entity.id!!) } returns Optional.of(entity)

        val result = countryService.getCountryById(entity.id!!)

        assertThat(result?.code).isEqualTo(entity.code)
        verify { countryRepository.findById(entity.id!!) }
    }

    @Test
    fun create_country_throws_when_code_exists() {
        val createDto = TestDataHelpers.createCountryCreateDto()
        every { countryRepository.findByCode(createDto.code) } returns countryEntity()

        assertThrows<ConflictException> {
            countryService.createCountry(createDto)
        }

        verify { countryRepository.findByCode(createDto.code) }
    }

    @Test
    fun create_country_saves_new_entity() {
        val createDto = TestDataHelpers.createCountryCreateDto()
        val saved = countryEntity(id = UUID.randomUUID(), code = createDto.code)
        every { countryRepository.findByCode(createDto.code) } returns null
        every { countryRepository.save(any()) } returns saved

        val result = countryService.createCountry(createDto)

        assertThat(result.code).isEqualTo(createDto.code)
        verify { countryRepository.save(any()) }
    }

    @Test
    fun update_country_saves_modified_entity() {
        val id = UUID.randomUUID()
        val entity = countryEntity(id = id, code = "US")
        val updateDto = TestDataHelpers.createCountryUpdateDto(nameRu = "Новая страна", nameEn = "New Country")
        every { countryRepository.findById(id) } returns Optional.of(entity)
        every { countryRepository.save(any()) } answers { firstArg() }

        val result = countryService.updateCountry(id, updateDto)

        assertThat(result.nameRu).isEqualTo("Новая страна")
        assertThat(result.nameEn).isEqualTo("New Country")
        verify { countryRepository.save(entity) }
    }

    @Test
    fun delete_country_returns_true_when_entity_exists() {
        val id = UUID.randomUUID()
        every { countryRepository.existsById(id) } returns true
        every { countryRepository.deleteById(id) } returns Unit

        val result = countryService.deleteCountry(id)

        assertThat(result).isTrue()
        verify { countryRepository.deleteById(id) }
    }

    private fun countryEntity(
        id: UUID = UUID.randomUUID(),
        code: String = "US",
        nameRu: String = "Соединенные Штаты",
        nameEn: String = "United States"
    ): CountryEntity =
        CountryEntity().apply {
            this.id = id
            this.code = code
            this.nameRu = nameRu
            this.nameEn = nameEn
        }

    private fun pageOf(entity: CountryEntity) =
        PageImpl(listOf(entity))
}
