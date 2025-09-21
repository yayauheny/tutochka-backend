package yayauheny.by.table

import org.jetbrains.exposed.dao.id.UUIDTable

object CitiesTable : UUIDTable("cities") {
    val country = reference("country_id", CountriesTable)
    val nameRu = varchar("name_ru", 255)
    val nameEn = varchar("name_en", 255)
    val region = varchar("region", 255).nullable()
    val lat = double("lat")
    val lon = double("lon")
}
