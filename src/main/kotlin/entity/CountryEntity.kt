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
    var name by CountriesTable.name

    fun toCountry(): Country {
        return Country(
            id = id.value,
            code = code,
            name = name
        )
    }
}
