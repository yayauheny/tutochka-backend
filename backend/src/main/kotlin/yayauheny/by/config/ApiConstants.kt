package yayauheny.by.config

/**
 * Centralized constants for API configuration, validation, and pagination.
 * All magic numbers and strings used throughout the application should be defined here.
 */
object ApiConstants {
    const val DEFAULT_PAGE_SIZE = 10
    const val MAX_PAGE_SIZE = 100
    const val DEFAULT_PAGE = 0

    const val MAX_NAME_LENGTH = 255
    const val MAX_DESCRIPTION_LENGTH = 255
    const val MAX_ADDRESS_LENGTH = 255
    const val MAX_REGION_LENGTH = 255
    const val MAX_COUNTRY_CODE_LENGTH = 10

    const val MIN_NAME_LENGTH = 2
    const val MIN_NAME_LENGTH_REQUIRED = 1

    const val DEFAULT_MAX_DISTANCE_METERS = 5000
    const val DEFAULT_MAX_NEAREST_RESTROOMS_SIZE = 5

    const val FILTER_PARTS_COUNT = 3
    const val FILTER_DELIMITER = ","
    const val FILTER_VALUE_DELIMITER = ":"

    const val MAX_JSON_STRING_LENGTH = 10000
}
