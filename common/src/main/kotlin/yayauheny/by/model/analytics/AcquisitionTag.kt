package yayauheny.by.model.analytics

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

enum class AcquisitionTag(val value: String) {
    @JsonProperty("direct")
    DIRECT("direct"),

    @JsonProperty("website")
    WEBSITE("website"),

    @JsonProperty("other")
    OTHER("other");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromValue(value: String?): AcquisitionTag? =
            entries.firstOrNull { it.value == value }
    }

    @JsonValue
    fun toJsonValue(): String = value
}
