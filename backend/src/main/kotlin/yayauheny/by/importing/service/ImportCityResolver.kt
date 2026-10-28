package yayauheny.by.importing.service

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import java.util.UUID

@Service
class ImportCityResolver(
    private val countryRepository: CountryJpaRepository,
    private val cityRepository: CityJpaRepository,
    private val entityManager: EntityManager
) {
    sealed class MetadataCityResolution {
        data class Resolved(val cityId: UUID) : MetadataCityResolution()
        data class NeedsNearestFallback(val countryId: UUID?, val reason: String) : MetadataCityResolution()
    }

    fun resolveByMetadata(countryName: String?, cityName: String?): MetadataCityResolution {
        val normalizedCountry = normalizeCountry(countryName)
        val normalizedCity = normalizeCity(cityName)
        if (normalizedCountry == null) {
            return MetadataCityResolution.NeedsNearestFallback(null, "country metadata is missing")
        }
        if (normalizedCity == null) {
            return MetadataCityResolution.NeedsNearestFallback(resolveCountryId(normalizedCountry), "city metadata is missing")
        }

        val countryId = resolveCountryId(normalizedCountry)
            ?: return MetadataCityResolution.NeedsNearestFallback(null, "country metadata is unknown")

        val cityId = cityRepository.findAll()
            .asSequence()
            .filter { it.country?.id == countryId }
            .firstOrNull { city ->
                normalizeCity(city.nameRu) == normalizedCity ||
                    normalizeCity(city.nameEn) == normalizedCity
            }
            ?.id

        return if (cityId != null) {
            MetadataCityResolution.Resolved(cityId)
        } else {
            MetadataCityResolution.NeedsNearestFallback(countryId, "city metadata did not match a known city")
        }
    }

    fun resolveNearest(lat: Double?, lng: Double?, countryId: UUID?): UUID? {
        if (lat == null || lng == null) {
            return null
        }

        val sql =
            buildString {
                append(
                    """
                    SELECT id
                    FROM cities
                    """.trimIndent()
                )
                if (countryId != null) {
                    append("\nWHERE country_id = CAST(:countryId AS uuid)")
                }
                append(
                    """
                    
                    ORDER BY ST_Distance(
                        coordinates::geography,
                        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
                    )
                    LIMIT 1
                    """.trimIndent()
                )
            }

        val query = entityManager.createNativeQuery(sql)
            .setParameter("lat", lat)
            .setParameter("lng", lng)
        if (countryId != null) {
            query.setParameter("countryId", countryId)
        }

        val result = query.resultList.firstOrNull() ?: return null
        return when (result) {
            is UUID -> result
            else -> UUID.fromString(result.toString())
        }
    }

    private fun resolveCountryId(normalizedCountry: String): UUID? =
        countryRepository.findAll()
            .asSequence()
            .firstOrNull { country ->
                val names =
                    listOfNotNull(
                        normalizeCountry(country.code),
                        normalizeCountry(country.nameRu),
                        normalizeCountry(country.nameEn)
                    )
                normalizedCountry in names
            }
            ?.id

    private fun normalizeCountry(value: String?): String? =
        normalizeToken(value)
            ?.removePrefix("republic ")
            ?.removePrefix("republic of ")
            ?.removePrefix("республика ")
            ?.removePrefix("respublika ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun normalizeCity(value: String?): String? =
        normalizeToken(value)
            ?.replace(LOCALITY_PREFIX_REGEX, "")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun normalizeToken(value: String?): String? =
        value
            ?.trim()
            ?.lowercase()
            ?.replace('ё', 'е')
            ?.replace(NON_ALNUM_REGEX, " ")
            ?.replace(MULTISPACE_REGEX, " ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private companion object {
        val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{Nd}]+")
        val MULTISPACE_REGEX = Regex("\\s+")
        val LOCALITY_PREFIX_REGEX =
            Regex("^(г\\.?|город|city|town|village|деревня|пос\\.?|поселок|посёлок|с\\.?|село)\\s+")
    }
}
