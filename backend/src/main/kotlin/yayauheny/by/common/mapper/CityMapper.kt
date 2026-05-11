package yayauheny.by.common.mapper

import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.tables.references.CITIES

object CityMapper {
    fun mapFromRecord(record: Record): CityResponseDto {
        val lat = requireNotNull(record.get("lat", Double::class.javaObjectType)) { "City latitude is required" }
        val lon = requireNotNull(record.get("lon", Double::class.javaObjectType)) { "City longitude is required" }
        return CityResponseDto(
            id = record[CITIES.ID]!!,
            countryId = record[CITIES.COUNTRY_ID]!!,
            nameRu = record[CITIES.NAME_RU]!!,
            nameEn = record[CITIES.NAME_EN]!!,
            region = record[CITIES.REGION],
            coordinates = Coordinates(lat = lat, lon = lon)
        )
    }

    fun applyUpdateDto(
        update: UpdateSetFirstStep<*>,
        dto: CityUpdateDto
    ): UpdateSetMoreStep<*> {
        return update
            .set(CITIES.COUNTRY_ID, dto.countryId)
            .set(CITIES.NAME_RU, dto.nameRu)
            .set(CITIES.NAME_EN, dto.nameEn)
            .set(CITIES.REGION, dto.region)
    }
}
