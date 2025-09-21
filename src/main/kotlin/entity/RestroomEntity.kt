package yayauheny.by.entity

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import yayauheny.by.model.NearestRestroomResponseDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.table.RestroomsTable

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

    fun toRestroomResponseDto(): RestroomResponseDto =
        RestroomResponseDto(
            id = id.value,
            cityId = city?.id?.value,
            name = name,
            description = description,
            address = address,
            phones = phones,
            workTime = workTime,
            feeType = feeType,
            accessibilityType = accessibilityType,
            lat = coordinates.latitude,
            lon = coordinates.longitude,
            dataSource = dataSource,
            status = status,
            amenities = amenities,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun toNearestRestroomResponseDto(distanceMeters: Double): NearestRestroomResponseDto =
        NearestRestroomResponseDto(
            id = id.value,
            cityId = city?.id?.value,
            name = name,
            description = description,
            address = address,
            phones = phones,
            workTime = workTime,
            feeType = feeType,
            accessibilityType = accessibilityType,
            lat = coordinates.latitude,
            lon = coordinates.longitude,
            dataSource = dataSource,
            status = status,
            amenities = amenities,
            createdAt = createdAt,
            updatedAt = updatedAt,
            distanceMeters = distanceMeters
        )
}
