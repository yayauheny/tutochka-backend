package yayauheny.by.common.mapper

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Polygon
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.tables.records.CitiesRecord
import yayauheny.by.tables.references.CITIES
import yayauheny.by.util.toPoint

object CityMapper {
    fun mapFromRecord(record: Record): CityResponseDto {
        val coordinates = record[CITIES.COORDINATES] as Point?
        return CityResponseDto(
            id = record[CITIES.ID]!!,
            cityBounds = record[CITIES.CITY_BOUNDS] as Polygon?,
            countryId = record[CITIES.COUNTRY_ID]!!,
            nameRu = record[CITIES.NAME_RU]!!,
            nameEn = record[CITIES.NAME_EN]!!,
            region = record[CITIES.REGION],
            lat = coordinates?.y ?: 0.0,
            lon = coordinates?.x ?: 0.0
        )
    }

    fun mapToSaveRecord(
        ctx: DSLContext,
        dto: CityCreateDto
    ): CitiesRecord {
        val record = ctx.newRecord(CITIES)
        val coordinates = (dto.lat to dto.lon).toPoint()
        record.countryId = dto.countryId
        record.nameRu = dto.nameRu
        record.nameEn = dto.nameEn
        record.region = dto.region
        record.coordinates = coordinates
        record.cityBounds = dto.cityBounds
        return record
    }

    fun mapToUpdateRecord(
        record: CitiesRecord,
        dto: CityUpdateDto
    ): CitiesRecord {
        val coordinates = (dto.lat to dto.lon).toPoint()
        record.countryId = dto.countryId
        record.nameRu = dto.nameRu
        record.nameEn = dto.nameEn
        record.region = dto.region
        record.coordinates = coordinates
        record.cityBounds = dto.cityBounds
        return record
    }

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: CityUpdateDto
    ): UpdateSetMoreStep<*> {
        val coordinates = (dto.lat to dto.lon).toPoint()
        return updateStep
            .set(CITIES.COUNTRY_ID, dto.countryId)
            .set(CITIES.NAME_RU, dto.nameRu)
            .set(CITIES.NAME_EN, dto.nameEn)
            .set(CITIES.REGION, dto.region)
            .set(CITIES.COORDINATES, coordinates)
            .set(CITIES.CITY_BOUNDS, dto.cityBounds)
    }
}
