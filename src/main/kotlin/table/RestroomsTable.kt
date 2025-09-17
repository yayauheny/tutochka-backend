package yayauheny.by.table

import java.time.Instant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.repository.type.GeographyPointColumnType

object RestroomsTable : UUIDTable("restrooms") {
    val cityId = reference("city_id", CitiesTable).nullable()
    val name = varchar("name", 255).nullable()
    val description = varchar("description", 255).nullable()
    val address = varchar("address", 255)
    val phones = jsonb<Map<String, Any>>("phones", Json).nullable()
    val workTime = jsonb<Map<String, Any>>("work_time", Json).nullable()
    val feeType = enumerationByName("fee_type", 20, FeeType::class)
    val accessibilityType = enumerationByName("accessibility_type", 20, AccessibilityType::class)
    val coordinates = registerColumn("coordinates", GeographyPointColumnType())
    val dataSource = enumerationByName("data_source", 20, DataSourceType::class)
    val status = enumerationByName("status", 20, RestroomStatus::class)
    val amenities = jsonb<Map<String, Any>>("amenities", Json)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}