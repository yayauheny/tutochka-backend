package yayauheny.by.model.restroom

import by.yayauheny.shared.dto.NearestRestroomResponseDto as SharedNearestRestroomResponseDto

/**
 * Backend-specific wrapper for NearestRestroomResponseDto with Swagger annotations.
 * Uses shared DTO internally to avoid duplication.
 */
typealias NearestRestroomResponseDto = SharedNearestRestroomResponseDto
