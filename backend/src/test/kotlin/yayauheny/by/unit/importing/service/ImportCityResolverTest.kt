package yayauheny.by.unit.importing.service

import io.mockk.every
import io.mockk.slot
import io.mockk.mockk
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.importing.service.ImportCityResolver
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import java.util.UUID
import yayauheny.by.util.point

class ImportCityResolverTest {
    private val countryRepository = mockk<CountryJpaRepository>()
    private val cityRepository = mockk<CityJpaRepository>()
    private val entityManager = mockk<EntityManager>()
    private val resolver = ImportCityResolver(countryRepository, cityRepository, entityManager)

    @Test
    fun `given matching country and city metadata when resolveByMetadata then return resolved city`() {
        val countryId = UUID.randomUUID()
        val cityId = UUID.randomUUID()
        every { countryRepository.findAll() } returns listOf(country(countryId, "BY", "Беларусь", "Belarus"))
        every { cityRepository.findAll() } returns listOf(city(cityId, countryId, "Минск", "Minsk"))

        val result = resolver.resolveByMetadata("Республика Беларусь", "г. Минск")

        assertThat(result).isEqualTo(ImportCityResolver.MetadataCityResolution.Resolved(cityId))
    }

    @Test
    fun `given unknown city metadata when resolveByMetadata then request nearest fallback with country id`() {
        val countryId = UUID.randomUUID()
        every { countryRepository.findAll() } returns listOf(country(countryId, "BY", "Беларусь", "Belarus"))
        every { cityRepository.findAll() } returns emptyList()

        val result = resolver.resolveByMetadata("Беларусь", "Несвиж")

        assertThat(result)
            .isEqualTo(
                ImportCityResolver.MetadataCityResolution.NeedsNearestFallback(
                    countryId = countryId,
                    reason = "city metadata did not match a known city"
                )
            )
    }

    @Test
    fun `given coordinates and country id when resolveNearest then return nearest city id from query`() {
        val cityId = UUID.randomUUID()
        val query = mockk<Query>()
        val sql = slot<String>()
        every { entityManager.createNativeQuery(capture(sql)) } returns query
        every { query.setParameter(any<String>(), any()) } returns query
        every { query.resultList } returns listOf(cityId)

        val result = resolver.resolveNearest(53.9, 27.56, UUID.randomUUID())

        assertThat(result).isEqualTo(cityId)
        assertThat(sql.captured).contains("WHERE country_id = CAST(:countryId AS uuid)")
    }

    private fun country(id: UUID, code: String, nameRu: String, nameEn: String): CountryEntity =
        CountryEntity().apply {
            this.id = id
            this.code = code
            this.nameRu = nameRu
            this.nameEn = nameEn
        }

    private fun city(id: UUID, countryId: UUID, nameRu: String, nameEn: String): CityEntity =
        CityEntity().apply {
            this.id = id
            this.country = country(countryId, "BY", "Беларусь", "Belarus")
            this.nameRu = nameRu
            this.nameEn = nameEn
            this.coordinates = point(27.56, 53.9)
        }
}
