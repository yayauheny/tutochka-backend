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
    private val countryRepository = mockk<CountryJpaRepository>()
    private val countryService = CountryService(countryRepository)

    // Read behavior tests

    @Test
    fun `getAllCountries maps entities to response dto`() {
        val pagination = PaginationRequest(page = 0, size = 10)
        val entity = countryEntity()
        every { countryRepository.findAllByFilter(any(), pagination) } returns pageOf(entity)

        val result = countryService.getAllCountries(pagination)

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().code).isEqualTo(entity.code)
        assertThat(result.content.single().nameRu).isEqualTo(entity.nameRu)
        assertThat(result.content.single().nameEn).isEqualTo(entity.nameEn)
        verify { countryRepository.findAllByFilter(CountryFilter(), pagination) }
    }

    @Test
    fun `getAllCountries with filter uses provided filter`() {
        val filter = CountryFilter(nameRu = "US")
        val pagination = PaginationRequest(page = 0, size = 10)
        val entity = countryEntity()
        every { countryRepository.findAllByFilter(any(), pagination) } returns pageOf(entity)

        val result = countryService.getAllCountries(filter, pagination)

        assertThat(result.content).hasSize(1)
        verify { countryRepository.findAllByFilter(filter, pagination) }
    }

    @Test
    fun `getCountryById returns mapped dto`() {
        val entity = countryEntity()
        every { countryRepository.findById(entity.id!!) } returns Optional.of(entity)

        val result = countryService.getCountryById(entity.id!!)

        assertThat(result?.code).isEqualTo(entity.code)
        assertThat(result?.nameRu).isEqualTo(entity.nameRu)
        assertThat(result?.nameEn).isEqualTo(entity.nameEn)
        verify { countryRepository.findById(entity.id!!) }
    }

    @Test
    fun `getCountryById returns null when absent`() {
        val id = UUID.randomUUID()
        every { countryRepository.findById(id) } returns Optional.empty()

        val result = countryService.getCountryById(id)

        assertThat(result).isNull()
    }

    @Test
    fun `getCountryByCode returns mapped dto`() {
        val code = "US"
        val entity = countryEntity(code = code)
        every { countryRepository.findByCode(code) } returns entity

        val result = countryService.getCountryByCode(code)

        assertThat(result?.code).isEqualTo(code)
        verify { countryRepository.findByCode(code) }
    }

    @Test
    fun `getCountryByCode returns null when absent`() {
        val code = "US"
        every { countryRepository.findByCode(code) } returns null

        val result = countryService.getCountryByCode(code)

        assertThat(result).isNull()
        verify { countryRepository.findByCode(code) }
    }

    @Test
    fun `countryExists returns true when found`() {
        val code = "US"
        every { countryRepository.findByCode(code) } returns countryEntity(code = code)

        val result = countryService.countryExists(code)

        assertThat(result).isTrue()
        verify { countryRepository.findByCode(code) }
    }

    @Test
    fun `countryExists returns false when absent`() {
        val code = "US"
        every { countryRepository.findByCode(code) } returns null

        val result = countryService.countryExists(code)

        assertThat(result).isFalse()
        verify { countryRepository.findByCode(code) }
    }

    // Create behavior tests

    @Test
    fun `createCountry throws when code exists`() {
        val createDto = TestDataHelpers.createCountryCreateDto()
        every { countryRepository.findByCode(createDto.code) } returns countryEntity()

        assertThrows<ConflictException> {
            countryService.createCountry(createDto)
        }

        verify { countryRepository.findByCode(createDto.code) }
    }

    @Test
    fun `createCountry saves new entity with mapped fields`() {
        val createDto = TestDataHelpers.createCountryCreateDto()
        val saved = countryEntity(id = UUID.randomUUID(), code = createDto.code, nameRu = createDto.nameRu, nameEn = createDto.nameEn)
        every { countryRepository.findByCode(createDto.code) } returns null
        
        val countrySlot = slot<CountryEntity>()
        every { countryRepository.save(capture(countrySlot)) } returns saved

        val result = countryService.createCountry(createDto)

        val captured = countrySlot.captured
        assertThat(captured.code).isEqualTo(createDto.code)
        assertThat(captured.nameRu).isEqualTo(createDto.nameRu)
        assertThat(captured.nameEn).isEqualTo(createDto.nameEn)
        assertThat(result.id).isEqualTo(saved.id)
        assertThat(result.code).isEqualTo(saved.code)
        assertThat(result.nameRu).isEqualTo(saved.nameRu)
        assertThat(result.nameEn).isEqualTo(saved.nameEn)
    }

    // Update behavior tests

    @Test
    fun `updateCountry saves modified entity`() {
        val id = UUID.randomUUID()
        val entity = countryEntity(id = id, code = "US")
        val updateDto = TestDataHelpers.createCountryUpdateDto(nameRu = "Новая страна", nameEn = "New Country")
        every { countryRepository.findById(id) } returns Optional.of(entity)
        
        val countrySlot = slot<CountryEntity>()
        every { countryRepository.save(capture(countrySlot)) } answers { firstArg() }

        val result = countryService.updateCountry(id, updateDto)

        assertThat(result.nameRu).isEqualTo("Новая страна")
        assertThat(result.nameEn).isEqualTo("New Country")
        
        val captured = countrySlot.captured
        assertThat(captured.nameRu).isEqualTo("Новая страна")
        assertThat(captured.nameEn).isEqualTo("New Country")
        verify { countryRepository.save(entity) }
    }

    @Test
    fun `updateCountry throws when id is missing`() {
        val id = UUID.randomUUID()
        val updateDto = TestDataHelpers.createCountryUpdateDto()
        every { countryRepository.findById(id) } returns Optional.empty()

        assertThrows<IllegalArgumentException> {
            countryService.updateCountry(id, updateDto)
        }
    }

    // Delete behavior tests

    @Test
    fun `deleteCountry returns true when entity exists`() {
        val id = UUID.randomUUID()
        every { countryRepository.existsById(id) } returns true
        every { countryRepository.deleteById(id) } returns Unit

        val result = countryService.deleteCountry(id)

        assertThat(result).isTrue()
        verify { countryRepository.deleteById(id) }
    }

    @Test
    fun `deleteCountry returns false when absent`() {
        val id = UUID.randomUUID()
        every { countryRepository.existsById(id) } returns false

        val result = countryService.deleteCountry(id)

        assertThat(result).isFalse()
        verify(exactly = 0) { countryRepository.deleteById(any()) }
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
