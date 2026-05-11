package yayauheny.by.analytics.model

import yayauheny.by.model.analytics.AnalyticsSource

data class AnalyticsIdentityHeaders(
    val tgUserId: Long?,
    val tgChatId: String?,
    val username: String?,
    val source: AnalyticsSource
)
