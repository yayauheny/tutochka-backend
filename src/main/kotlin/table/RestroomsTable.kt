package yayauheny.by.table

import java.time.Instant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.repository.type.GeographyPointColumnType

object RestroomsTable : UUIDTable("restrooms") {
    val cityId = reference("city_id", CitiesTable).nullable()
    val code = varchar("code", 255)
    val description = varchar("description", 255).nullable()
    val name = varchar("name", 255).nullable()
    val workTime = varchar("work_time", 255).nullable()
    val feeType = enumerationByName("fee_type", 20, FeeType::class)
    val accessibilityType = enumerationByName("fee_type", 20, AccessibilityType::class)
    val coordinates = registerColumn("coordinates", GeographyPointColumnType())
    val dataSource = enumerationByName("data_source", 20, DataSourceType::class)
    val amenities = jsonb<Map<String, Any>>("amenities", Json)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}