package yayauheny.by.common.mapper

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
import yayauheny.by.tables.records.CountriesRecord
import yayauheny.by.tables.references.COUNTRIES

object CountryMapper {
    fun mapFromRecord(record: Record): CountryResponseDto {
        return CountryResponseDto(
            id = record[COUNTRIES.ID]!!,
            code = record[COUNTRIES.CODE]!!,
            nameRu = record[COUNTRIES.NAME_RU]!!,
            nameEn = record[COUNTRIES.NAME_EN]!!
        )
    }

    fun mapToSaveRecord(
        ctx: DSLContext,
        dto: CountryCreateDto
    ): CountriesRecord {
        val record = ctx.newRecord(COUNTRIES)
        record.code = dto.code
        record.nameRu = dto.nameRu
        record.nameEn = dto.nameEn
        return record
    }

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: CountryUpdateDto
    ): UpdateSetMoreStep<*> {
        return updateStep
            .set(COUNTRIES.NAME_RU, dto.nameRu)
            .set(COUNTRIES.NAME_EN, dto.nameEn)
    }
}
