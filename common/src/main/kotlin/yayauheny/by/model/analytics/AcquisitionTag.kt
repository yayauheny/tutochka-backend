package yayauheny.by.model.analytics

import com.fasterxml.jackson.annotation.JsonProperty





enum class AcquisitionTag(val value: String) {
    @JsonProperty("direct")
    DIRECT("direct"),

    @JsonProperty("website")
    WEBSITE("website"),

    @JsonProperty("other")
    OTHER("other");

    companion object {
        fun fromValue(value: String?): AcquisitionTag? =
            entries.firstOrNull { it.value == value }
    }
}
