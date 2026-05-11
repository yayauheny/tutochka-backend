package yayauheny.by.analytics.api

import io.ktor.server.application.ApplicationCall
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.util.getRequestHeader

private const val HEADER_TG_USER_ID = "X-Tg-User-Id"
private const val HEADER_TG_CHAT_ID = "X-Tg-Chat-Id"
private const val HEADER_TG_USERNAME = "X-Tg-Username"
private const val HEADER_ANALYTICS_SOURCE = "X-Analytics-Source"

fun ApplicationCall.getAnalyticsIdentityHeaders(defaultSource: AnalyticsSource): AnalyticsIdentityHeaders =
    AnalyticsIdentityHeaders(
        tgUserId = getRequestHeader(HEADER_TG_USER_ID)?.toLongOrNull(),
        tgChatId = getRequestHeader(HEADER_TG_CHAT_ID),
        username = getRequestHeader(HEADER_TG_USERNAME),
        source = AnalyticsSource.fromValue(getRequestHeader(HEADER_ANALYTICS_SOURCE)) ?: defaultSource
    )
