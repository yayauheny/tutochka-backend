package yayauheny.by.entity

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import yayauheny.by.table.RestroomsTable
import yayauheny.by.util.toJsonObject
import java.util.UUID

class RestroomEntity(
    id: EntityID<UUID>
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RestroomEntity>(RestroomsTable)

    var city by CityEntity optionalReferencedOn RestroomsTable.cityId
    var name by RestroomsTable.name
    var description by RestroomsTable.description
    var address by RestroomsTable.address
    var phones by RestroomsTable.phones
    var workTime by RestroomsTable.workTime
    var feeType by RestroomsTable.feeType
    var accessibilityType by RestroomsTable.accessibilityType
    var coordinates by RestroomsTable.coordinates
    var dataSource by RestroomsTable.dataSource
    var status by RestroomsTable.status
    var amenities by RestroomsTable.amenities
    var createdAt by RestroomsTable.createdAt
    var updatedAt by RestroomsTable.updatedAt

    fun toRestroom(): RestroomEntity =
        RestroomEntity(
            id = id.value,
            cityId = city?.id?.value,
            name = name,
            description = description,
            address = address,
            phones = phones?.toJsonObject(),
            workTime = workTime?.toJsonObject(),
            feeType = feeType,
            accessibilityType = accessibilityType,
            coordinates = coordinates,
            dataSource = dataSource,
            status = status,
            amenities = amenities.toJsonObject(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
