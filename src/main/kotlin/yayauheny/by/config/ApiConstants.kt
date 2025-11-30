package yayauheny.by.config

/**
 * Centralized constants for API configuration, validation, and pagination.
 * All magic numbers and strings used throughout the application should be defined here.
 */
object ApiConstants {
    // Pagination constants
    const val DEFAULT_PAGE_SIZE = 10
    const val MAX_PAGE_SIZE = 100
    const val DEFAULT_PAGE = 0

    // Field length constraints
    const val MAX_NAME_LENGTH = 255
    const val MAX_DESCRIPTION_LENGTH = 255
    const val MAX_ADDRESS_LENGTH = 255
    const val MAX_REGION_LENGTH = 255
    const val MAX_COUNTRY_CODE_LENGTH = 10

    // Minimum length constraints
    const val MIN_NAME_LENGTH = 2
    const val MIN_NAME_LENGTH_REQUIRED = 1

    // Distance constants
    const val DEFAULT_MAX_DISTANCE_METERS = 1000

    // Filter parsing constants
    const val FILTER_PARTS_COUNT = 3
    const val FILTER_DELIMITER = ","
    const val FILTER_VALUE_DELIMITER = ":"
}
