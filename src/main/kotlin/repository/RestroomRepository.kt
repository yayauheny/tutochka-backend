package yayauheny.by.repository

import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.GeoPoint
import yayauheny.by.model.NearestRestroomResponseDto
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.repository.type.GeographyPointColumnType
import yayauheny.by.table.RestroomsTable

interface RestroomRepository {
    suspend fun findAll(pagination: PaginationDto): PageResponseDto<RestroomResponseDto>

    suspend fun findById(id: UUID): RestroomResponseDto?

    suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationDto
    ): PageResponseDto<RestroomResponseDto>

    suspend fun findNearestByLocation(
        latitude: Double,
        longitude: Double,
        limit: Int = 5
    ): List<NearestRestroomResponseDto>

    suspend fun save(restroom: RestroomResponseDto): RestroomResponseDto

    suspend fun deleteById(id: UUID): Boolean
}

class RestroomRepositoryImpl : RestroomRepository {
    override suspend fun findAll(pagination: PaginationDto): PageResponseDto<RestroomResponseDto> =
        newSuspendedTransaction {
            val totalCount = RestroomEntity.all().count()
            val totalPages =
                if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size

            val content =
                RestroomEntity
                    .all()
                    .limit(pagination.size)
                    .offset(offset.toLong())
                    .map { it.toRestroomResponseDto() }

            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
        }

    override suspend fun findById(id: UUID): RestroomResponseDto? =
        newSuspendedTransaction {
            RestroomEntity.findById(id)?.toRestroomResponseDto()
        }

    override suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationDto
    ): PageResponseDto<RestroomResponseDto> =
        newSuspendedTransaction {
            val query = RestroomEntity.find { RestroomsTable.cityId eq cityId }
            val totalCount = query.count()
            val totalPages =
                if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size

            val content =
                query
                    .limit(pagination.size)
                    .offset(offset.toLong())
                    .map { it.toRestroomResponseDto() }

            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
        }

    override suspend fun findNearestByLocation(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): List<NearestRestroomResponseDto> =
        newSuspendedTransaction {
            val sql =
                """
                SELECT id, city_id, name, description, address, phones, work_time,
                       fee_type, accessibility_type, coordinates, data_source, status, amenities,
                       created_at, updated_at,
                       ST_Distance(coordinates, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) AS distance_meters
                FROM restrooms
                WHERE status = ?
                ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                LIMIT ?
                """.trimIndent()

            val args =
                listOf(
                    DoubleColumnType() to longitude, // 1
                    DoubleColumnType() to latitude, // 2
                    RestroomsTable.status.columnType to RestroomStatus.ACTIVE, // 3
                    DoubleColumnType() to longitude, // 4
                    DoubleColumnType() to latitude, // 5
                    IntegerColumnType() to limit // 6
                )

            exec(sql, args) { rs ->
                val geoType = GeographyPointColumnType()
                val out = mutableListOf<NearestRestroomResponseDto>()
                while (rs.next()) {
                    val gp: GeoPoint = geoType.valueFromDB(rs.getObject("coordinates")) ?: GeoPoint(0.0, 0.0)
                    val phonesStr = rs.getString("phones")
                    val workTimeStr = rs.getString("work_time")
                    val amenitiesStr = rs.getString("amenities")

                    out.add(
                        NearestRestroomResponseDto(
                            id = rs.getObject("id", java.util.UUID::class.java),
                            cityId = rs.getObject("city_id", java.util.UUID::class.java),
                            name = rs.getString("name"),
                            description = rs.getString("description"),
                            address = rs.getString("address"),
                            phones = phonesStr?.let { Json.parseToJsonElement(it) as? JsonObject },
                            workTime = workTimeStr?.let { Json.parseToJsonElement(it) as? JsonObject },
                            feeType = FeeType.valueOf(rs.getString("fee_type")),
                            accessibilityType = AccessibilityType.valueOf(rs.getString("accessibility_type")),
                            lat = gp.latitude,
                            lon = gp.longitude,
                            dataSource = DataSourceType.valueOf(rs.getString("data_source")),
                            status = RestroomStatus.valueOf(rs.getString("status")),
                            amenities = amenitiesStr?.let { Json.parseToJsonElement(it) as? JsonObject } ?: JsonObject(emptyMap()),
                            createdAt = rs.getTimestamp("created_at").toInstant(),
                            updatedAt = rs.getTimestamp("updated_at").toInstant(),
                            distanceMeters = rs.getDouble("distance_meters")
                        )
                    )
                }
                out
            } ?: emptyList()
        }

    override suspend fun save(restroom: RestroomResponseDto): RestroomResponseDto =
        newSuspendedTransaction {
            val entity = RestroomEntity.findById(restroom.id) ?: createNewEntity(restroom)
            entity.updateFromDto(restroom)
            entity.toRestroomResponseDto()
        }

    override suspend fun deleteById(id: UUID): Boolean =
        newSuspendedTransaction {
            RestroomEntity.findById(id)?.delete() != null
        }

    private fun createNewEntity(dto: RestroomResponseDto) =
        RestroomEntity.new {
            updateFromDto(dto)
        }

    private fun RestroomEntity.updateFromDto(dto: RestroomResponseDto) {
        name = dto.name
        description = dto.description
        address = dto.address
        phones = dto.phones
        workTime = dto.workTime
        feeType = dto.feeType
        accessibilityType = dto.accessibilityType
        coordinates = GeoPoint(dto.lon, dto.lat)
        dataSource = dto.dataSource
        status = dto.status
        amenities = dto.amenities
    }
}
