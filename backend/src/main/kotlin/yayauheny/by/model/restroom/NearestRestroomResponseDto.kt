package yayauheny.by.model.restroom

import by.yayauheny.shared.dto.NearestRestroomResponseDto as SharedNearestRestroomResponseDto
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Backend-specific wrapper for NearestRestroomResponseDto with Swagger annotations.
 * Uses shared DTO internally to avoid duplication.
 */
typealias NearestRestroomResponseDto = SharedNearestRestroomResponseDto
