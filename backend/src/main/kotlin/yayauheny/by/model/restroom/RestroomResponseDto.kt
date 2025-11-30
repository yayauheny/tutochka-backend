package yayauheny.by.model.restroom

import by.yayauheny.shared.dto.RestroomResponseDto as SharedRestroomResponseDto

/**
 * Backend-specific wrapper for RestroomResponseDto with Swagger annotations.
 * Uses shared DTO internally to avoid duplication.
 */
typealias RestroomResponseDto = SharedRestroomResponseDto
