package yayauheny.by.table

import org.jetbrains.exposed.dao.id.UUIDTable

object CountriesTable : UUIDTable("countries") {
    val code = varchar("code", 10)
    val name = varchar("name", 255)
}