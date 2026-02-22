package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class LocationType {
    STANDALONE,
    INSIDE_BUILDING,
    UNKNOWN
}
