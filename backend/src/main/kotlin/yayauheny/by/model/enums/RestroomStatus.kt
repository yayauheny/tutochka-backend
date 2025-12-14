package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class RestroomStatus {
    ACTIVE,
    INACTIVE,
    PENDING,
    TEMP_CLOSED
}
