package yayauheny.by.entity

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import yayauheny.by.model.Country
import yayauheny.by.table.CountriesTable

class CountryEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CountryEntity>(CountriesTable)

    var code by CountriesTable.code
    var nameRu by CountriesTable.nameRu
    var nameEn by CountriesTable.nameEn

    fun toCountry(): Country {
        return Country(
            id = id.value,
            code = code,
            nameRu = nameRu,
            nameEn = nameEn
        )
    }
}
