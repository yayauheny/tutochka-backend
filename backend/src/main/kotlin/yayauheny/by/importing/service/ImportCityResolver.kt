package yayauheny.by.importing.service

import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import yayauheny.by.tables.references.CITIES
import yayauheny.by.tables.references.COUNTRIES
import yayauheny.by.util.knnOrderTo

class ImportCityResolver(
    private val ctx: DSLContext
) {
    sealed interface MetadataCityResolution {
        data class Resolved(
            val cityId: UUID
        ) : MetadataCityResolution

        data class NeedsNearestFallback(
            val countryId: UUID?,
            val reason: String
        ) : MetadataCityResolution
    }

    suspend fun resolveByMetadata(
        countryName: String,
        cityName: String
    ): MetadataCityResolution =
        withContext(Dispatchers.IO) {
            val countryMatches = findCountries(countryName)
            if (countryMatches.size != 1) {
                return@withContext MetadataCityResolution.NeedsNearestFallback(
                    countryId = null,
                    reason = "country lookup matched ${countryMatches.size} records"
                )
            }

            val countryId = countryMatches.single()
            val cityMatches = findCities(countryId, cityName)
            when (cityMatches.size) {
                1 ->
                    MetadataCityResolution.Resolved(
                        cityId = cityMatches.single()
                    )

                0 ->
                    MetadataCityResolution.NeedsNearestFallback(
                        countryId = countryId,
                        reason = "city lookup matched 0 records"
                    )

                else ->
                    MetadataCityResolution.NeedsNearestFallback(
                        countryId = countryId,
                        reason = "city lookup matched ${cityMatches.size} records"
                    )
            }
        }

    suspend fun resolveNearest(
        lat: Double?,
        lng: Double?,
        countryId: UUID?
    ): UUID? =
        withContext(Dispatchers.IO) {
            if (lat == null || lng == null) {
                return@withContext null
            }

            val query =
                ctx
                    .select(CITIES.ID)
                    .from(CITIES)
                    .where(CITIES.IS_DELETED.isFalse)
                    .and(CITIES.COORDINATES.isNotNull)
                    .apply {
                        if (countryId != null) {
                            and(CITIES.COUNTRY_ID.eq(countryId))
                        }
                    }.orderBy(CITIES.COORDINATES.knnOrderTo(lat, lng))
                    .limit(1)

            query.fetchOne(CITIES.ID)
        }

    private fun findCountries(countryName: String): List<UUID> {
        val normalizedInput = normalizeCountryName(countryName) ?: return emptyList()
        val escapedName = escapeLike(countryName.trim())
        return ctx
            .select(COUNTRIES.ID, COUNTRIES.CODE, COUNTRIES.NAME_RU, COUNTRIES.NAME_EN)
            .from(COUNTRIES)
            .where(COUNTRIES.IS_DELETED.isFalse)
            .and(
                COUNTRIES.CODE
                    .eq(countryName.trim().uppercase())
                    .or(COUNTRIES.NAME_RU.likeIgnoreCase("%$escapedName%").escape('\\'))
                    .or(COUNTRIES.NAME_EN.likeIgnoreCase("%$escapedName%").escape('\\'))
            ).fetch()
            .mapNotNull { record ->
                val countryId = record.get(COUNTRIES.ID) ?: return@mapNotNull null
                val code = record.get(COUNTRIES.CODE)
                val nameRu = record.get(COUNTRIES.NAME_RU)
                val nameEn = record.get(COUNTRIES.NAME_EN)
                if (
                    code.equals(countryName.trim(), ignoreCase = true) ||
                    normalizeCountryName(nameRu) == normalizedInput ||
                    normalizeCountryName(nameEn) == normalizedInput
                ) {
                    countryId
                } else {
                    null
                }
            }.distinct()
    }

    private fun findCities(
        countryId: UUID,
        cityName: String
    ): List<UUID> {
        val normalizedInput = normalizeCityName(cityName) ?: return emptyList()
        val escapedName = escapeLike(cityName.trim())
        return ctx
            .select(CITIES.ID, CITIES.NAME_RU, CITIES.NAME_EN)
            .from(CITIES)
            .where(CITIES.IS_DELETED.isFalse)
            .and(CITIES.COUNTRY_ID.eq(countryId))
            .and(
                CITIES.NAME_RU
                    .likeIgnoreCase("%$escapedName%")
                    .escape('\\')
                    .or(CITIES.NAME_EN.likeIgnoreCase("%$escapedName%").escape('\\'))
            ).fetch()
            .mapNotNull { record ->
                val cityId = record.get(CITIES.ID) ?: return@mapNotNull null
                val nameRu = record.get(CITIES.NAME_RU)
                val nameEn = record.get(CITIES.NAME_EN)
                if (
                    normalizeCityName(nameRu) == normalizedInput ||
                    normalizeCityName(nameEn) == normalizedInput
                ) {
                    cityId
                } else {
                    null
                }
            }.distinct()
    }

    private fun normalizeCountryName(value: String?): String? {
        val normalized = collapseWhitespace(value)?.lowercase() ?: return null
        val aliased = COUNTRY_ALIASES[normalized] ?: normalized
        return aliased.replace(REPUBLIC_PREFIX, "").trim().takeIf { it.isNotEmpty() }
    }

    private fun normalizeCityName(value: String?): String? =
        collapseWhitespace(value)
            ?.replace(LOCALITY_PREFIX, "")
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }

    private fun collapseWhitespace(value: String?): String? =
        value
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.takeIf { it.isNotEmpty() }

    private fun escapeLike(value: String): String = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private companion object {
        val REPUBLIC_PREFIX = Regex("^(республика|republic of|republic)\\s+", RegexOption.IGNORE_CASE)
        val LOCALITY_PREFIX =
            Regex("^(г\\.?|город|д\\.?|деревня|пос\\.?|поселок|посёлок|пгт|с\\.?|село|аг\\.?|агрогородок)\\s+", RegexOption.IGNORE_CASE)
        val COUNTRY_ALIASES =
            mapOf(
                "республика беларусь" to "беларусь",
                "белоруссия" to "беларусь",
                "republic of belarus" to "belarus",
                "republic belarus" to "belarus",
                "российская федерация" to "россия",
                "russian federation" to "russia"
            )
    }
}
