package yayauheny.by.contract

import java.util.Optional
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto

interface BackendClient {
    fun findNearest(
        lat: Double,
        lon: Double,
        limit: Int,
        distanceMeters: Int
    ): List<NearestRestroomSlimDto>

    fun getById(id: String): Optional<RestroomResponseDto>
}
