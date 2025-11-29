package yayauheny.by.common.mapper

import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import org.slf4j.LoggerFactory
import yayauheny.by.model.LatLon
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.tables.references.CITIES

object CityMapper {
    private val logger = LoggerFactory.getLogger(CityMapper::class.java)

    fun mapFromRecord(record: Record): CityResponseDto {
        logger.info("mapFromRecord() called")
        try {
            logger.info("Getting lat/lon from record")
            val lat = record[CITIES.LAT]
            val lon = record[CITIES.LON]
            logger.info("lat=$lat, lon=$lon")

            require(lat != null && lon != null) {
                "Missing lat/lon in record. Make sure query/returning uses cityProjection() with CITIES.LAT/LON."
            }
            logger.info("lat/lon validation passed")

            logger.info("Getting other fields from record")
            val id = record[CITIES.ID]!!
            val countryId = record[CITIES.COUNTRY_ID]!!
            val nameRu = record[CITIES.NAME_RU]!!
            val nameEn = record[CITIES.NAME_EN]!!
            val region = record[CITIES.REGION]
            logger.info("Fields extracted: id=$id, countryId=$countryId, nameRu=$nameRu, nameEn=$nameEn, region=$region")

            logger.info("Creating CityResponseDto")
            val result =
                CityResponseDto(
                    id = id,
                    countryId = countryId,
                    nameRu = nameRu,
                    nameEn = nameEn,
                    region = region,
                    coordinates = LatLon(lat = lat, lon = lon)
                )
            logger.info("CityResponseDto created successfully: id=${result.id}")
            return result
        } catch (e: Exception) {
            logger.error("Error in mapFromRecord()", e)
            throw e
        }
    }

    fun applyUpdateDto(
        update: UpdateSetFirstStep<*>,
        dto: CityUpdateDto
    ): UpdateSetMoreStep<*> {
        val r = CITIES
        return update
            .set(r.COUNTRY_ID, dto.countryId)
            .set(r.NAME_RU, dto.nameRu)
            .set(r.NAME_EN, dto.nameEn)
            .set(r.REGION, dto.region)
    }
}
