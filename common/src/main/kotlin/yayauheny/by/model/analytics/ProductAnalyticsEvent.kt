package yayauheny.by.model.analytics

import com.fasterxml.jackson.annotation.JsonProperty





enum class ProductAnalyticsEvent(val value: String) {
    @JsonProperty("bot_started")
    BOT_STARTED("bot_started"),

    @JsonProperty("nearest_restrooms_returned")
    NEAREST_RESTROOMS_RETURNED("nearest_restrooms_returned"),

    @JsonProperty("nearest_restrooms_no_results")
    NEAREST_RESTROOMS_NO_RESULTS("nearest_restrooms_no_results"),

    @JsonProperty("restroom_details_opened")
    RESTROOM_DETAILS_OPENED("restroom_details_opened"),

    @JsonProperty("route_clicked")
    ROUTE_CLICKED("route_clicked")
}
