package yayauheny.by.entity

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import yayauheny.by.entity.RestroomEntity.Companion.referrersOn
import yayauheny.by.table.CitiesTable
import yayauheny.by.table.RestroomsTable
import yayauheny.by.util.toJsonObject

class RestroomEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RestroomEntity>(RestroomsTable)

    var city by CityEntity optionalReferencedOn RestroomsTable.cityId
    var code by RestroomsTable.code
    var description by RestroomsTable.description
    var name by RestroomsTable.name
    var workTime by RestroomsTable.workTime
    var feeType by RestroomsTable.feeType
    var accessibilityType by RestroomsTable.accessibilityType
    var coordinates by RestroomsTable.coordinates
    var dataSource by RestroomsTable.dataSource
    var amenities by RestroomsTable.amenities
    var createdAt by RestroomsTable.createdAt
    var updatedAt by RestroomsTable.updatedAt

    fun toRestroom(): Restroom {
        return Restroom(
            id = id.value,
            cityId = city?.id?.value,
            code = code,
            description = description,
            name = name,
            workTime = workTime,
            feeType = feeType,
            accessibilityType = accessibilityType,
            coordinates = coordinates,
            dataSource = dataSource,
            amenities = amenities.toJsonObject(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
