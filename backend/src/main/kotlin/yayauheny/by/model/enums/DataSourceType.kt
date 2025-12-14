package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class DataSourceType {
    MANUAL,
    USER,
    API,
    IMPORT
}
