package yayauheny.by.entity

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import yayauheny.by.model.CityResponseDto
import yayauheny.by.table.CitiesTable
import java.util.UUID

class CityEntity(
    id: EntityID<UUID>
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CityEntity>(CitiesTable)

    var country by CountryEntity referencedOn CitiesTable.country
    var nameRu by CitiesTable.nameRu
    var nameEn by CitiesTable.nameEn
    var region by CitiesTable.region
    var lat by CitiesTable.lat
    var lon by CitiesTable.lon

    fun toResponseDto(): CityResponseDto =
        CityResponseDto(
            id = id.value,
            countryId = country.id.value,
            nameRu = nameRu,
            nameEn = nameEn,
            region = region,
            lat = lat,
            lon = lon
        )
}
