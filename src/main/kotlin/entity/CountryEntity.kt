package yayauheny.by.entity

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import yayauheny.by.table.CountriesTable
import java.util.UUID

class CountryEntity(
    id: EntityID<UUID>
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CountryEntity>(CountriesTable)

    var code by CountriesTable.code
    var nameRu by CountriesTable.nameRu
    var nameEn by CountriesTable.nameEn
}
