package yayauheny.by.model.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProductAnalyticsEvent(val value: String) {
    @SerialName("nearest_restrooms_returned")
    NEAREST_RESTROOMS_RETURNED("nearest_restrooms_returned"),

    @SerialName("nearest_restrooms_no_results")
    NEAREST_RESTROOMS_NO_RESULTS("nearest_restrooms_no_results"),

    @SerialName("restroom_details_opened")
    RESTROOM_DETAILS_OPENED("restroom_details_opened"),

    @SerialName("route_clicked")
    ROUTE_CLICKED("route_clicked")
}
