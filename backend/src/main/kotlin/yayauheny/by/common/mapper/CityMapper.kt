package yayauheny.by.common.mapper

import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import by.yayauheny.shared.dto.LatLon
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.tables.references.CITIES

object CityMapper {
    fun mapFromRecord(record: Record): CityResponseDto {
        val lat = record.get("lat", Double::class.javaObjectType)
        val lon = record.get("lon", Double::class.javaObjectType)

        // Если вдруг не пришли — шанс, что забыли подключить projection:
        require(lat != null && lon != null) {
            "Missing lat/lon in record. Make sure query/returning uses cityProjection() with CITIES.COORDINATES.lat()/lon()."
        }

        val id = record[CITIES.ID]!!
        val countryId = record[CITIES.COUNTRY_ID]!!
        val nameRu = record[CITIES.NAME_RU]!!
        val nameEn = record[CITIES.NAME_EN]!!
        val region = record[CITIES.REGION]

        return CityResponseDto(
            id = id,
            countryId = countryId,
            nameRu = nameRu,
            nameEn = nameEn,
            region = region,
            coordinates = LatLon(lat = lat, lon = lon)
        )
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
