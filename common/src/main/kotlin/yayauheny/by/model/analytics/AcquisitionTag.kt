package yayauheny.by.model.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AcquisitionTag(val value: String) {
    @SerialName("direct")
    DIRECT("direct"),

    @SerialName("website")
    WEBSITE("website"),

    @SerialName("other")
    OTHER("other");

    companion object {
        fun fromValue(value: String?): AcquisitionTag? =
            entries.firstOrNull { it.value == value }
    }
}
