package yayauheny.by.table

import org.jetbrains.exposed.dao.id.UUIDTable

object CountriesTable : UUIDTable("countries") {
    val code = varchar("code", 10)
    val nameRu = varchar("name_ru", 255)
    val nameEn = varchar("name_en", 255)
}