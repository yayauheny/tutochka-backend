package yayauheny.by.common.mapper

import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.LatLon
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.tables.references.CITIES
import yayauheny.by.util.geomFromGeoJson
import yayauheny.by.util.reqDouble

object CityMapper {
    fun mapFromRecord(record: Record): CityResponseDto {
        return CityResponseDto(
            id = record[CITIES.ID]!!,
            // если хотите GeoJSON вместо toString(), можно в select’е вернуть ST_AsGeoJSON(...)
            cityBounds = record.get("city_bounds_json", String::class.java),
            countryId = record[CITIES.COUNTRY_ID]!!,
            nameRu = record[CITIES.NAME_RU]!!,
            nameEn = record[CITIES.NAME_EN]!!,
            region = record[CITIES.REGION],
            coordinates =
                LatLon(
                    lat = record.reqDouble("lat"),
                    lon = record.reqDouble("lon")
                )
        )
    }

    fun applyUpdateDto(
        update: UpdateSetFirstStep<*>,
        dto: CityUpdateDto
    ): UpdateSetMoreStep<*> {
        val r = CITIES
        var step =
            update
                .set(r.COUNTRY_ID, dto.countryId)
                .set(r.NAME_RU, dto.nameRu)
                .set(r.NAME_EN, dto.nameEn)
                .set(r.REGION, dto.region)

        // city_bounds как GeoJSON -> GEOMETRY
        step =
            if (dto.cityBounds != null) {
                step.set(
                    r.CITY_BOUNDS,
                    geomFromGeoJson(dto.cityBounds, r.CITY_BOUNDS)
                )
            } else {
                // Снять полигон
                step.set(r.CITY_BOUNDS, null as org.jooq.Geometry?)
            }

        return step
    }
}
