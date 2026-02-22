package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class AccessibilityType {
    INACCESSIBLE,
    WHEELCHAIR,
    CHANGING_PLACES,
    UNKNOWN
}
