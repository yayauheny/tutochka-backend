package by.yayauheny.shared.enums

import kotlinx.serialization.Serializable

@Serializable
enum class AccessibilityType {
    MEN,
    WOMEN,
    UNISEX,
    FAMILY,
    DISABLED,
    NONE
}
