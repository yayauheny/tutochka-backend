package yayauheny.by.analytics.api

import org.springframework.http.HttpHeaders
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.model.analytics.AnalyticsSource

private const val HEADER_TG_USER_ID = "X-Tg-User-Id"
private const val HEADER_TG_CHAT_ID = "X-Tg-Chat-Id"
private const val HEADER_TG_USERNAME = "X-Tg-Username"
private const val HEADER_ANALYTICS_SOURCE = "X-Analytics-Source"

fun HttpHeaders.getAnalyticsIdentityHeaders(defaultSource: AnalyticsSource): AnalyticsIdentityHeaders =
    AnalyticsIdentityHeaders(
        tgUserId = this.getFirst(HEADER_TG_USER_ID)?.toLongOrNull(),
        tgChatId = this.getFirst(HEADER_TG_CHAT_ID),
        username = this.getFirst(HEADER_TG_USERNAME),
        source = AnalyticsSource.fromValue(this.getFirst(HEADER_ANALYTICS_SOURCE)) ?: defaultSource
    )
