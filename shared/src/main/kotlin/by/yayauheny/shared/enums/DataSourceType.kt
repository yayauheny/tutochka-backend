package by.yayauheny.shared.enums

import kotlinx.serialization.Serializable

@Serializable
enum class DataSourceType {
    MANUAL,
    USER,
    API,
    IMPORT
}
