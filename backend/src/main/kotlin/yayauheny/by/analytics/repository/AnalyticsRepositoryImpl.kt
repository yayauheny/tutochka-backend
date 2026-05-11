package yayauheny.by.analytics.repository

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import yayauheny.by.analytics.model.EncryptedAnalyticsEventCommand
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.tables.references.ANALYTICS_EVENTS
import yayauheny.by.tables.references.USERS
import yayauheny.by.tables.references.USER_ANALYTICS
import yayauheny.by.util.toJSONB
import yayauheny.by.util.transactionSuspend

class AnalyticsRepositoryImpl(
    private val ctx: DSLContext
) : AnalyticsRepository {
    override suspend fun trackProductEvent(command: EncryptedAnalyticsEventCommand): Unit =
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                val userId = upsertUser(txCtx, command)

                if (userId != null) {
                    upsertUserAnalytics(txCtx, userId, command.event)
                }
                insertAnalyticsEvent(txCtx, userId, command)
            }
        }

    private fun upsertUser(
        txCtx: DSLContext,
        command: EncryptedAnalyticsEventCommand,
    ): UUID? {
        val tgUserId = command.tgUserId ?: return null
        val encryptedTgChatId = command.tgChatId ?: return null

        return txCtx
            .insertInto(USERS)
            .set(USERS.TG_USER_ID, tgUserId)
            .set(USERS.TG_CHAT_ID, encryptedTgChatId)
            .set(USERS.SOURCE, command.source.value)
            .set(USERS.USERNAME, command.username)
            .set(USERS.UPDATED_AT, Instant.now())
            .onConflict(USERS.TG_USER_ID)
            .doUpdate()
            .set(USERS.TG_CHAT_ID, encryptedTgChatId)
            .set(USERS.SOURCE, command.source.value)
            .set(USERS.UPDATED_AT, Instant.now())
            .apply {
                if (command.username != null) {
                    set(USERS.USERNAME, command.username)
                }
            }.returning(USERS.ID)
            .fetchOne()
            ?.getValue(USERS.ID)
            ?: error("Failed to upsert user analytics")
    }

    private fun upsertUserAnalytics(
        txCtx: DSLContext,
        userId: UUID,
        event: ProductAnalyticsEvent
    ) {
        val now = Instant.now()
        val counters = event.analyticsCounters()

        txCtx
            .insertInto(USER_ANALYTICS)
            .set(USER_ANALYTICS.USER_ID, userId)
            .set(USER_ANALYTICS.SEARCHES_COUNT, counters.searchesCount)
            .set(USER_ANALYTICS.SUCCESSFUL_SEARCHES_COUNT, counters.successfulSearchesCount)
            .set(USER_ANALYTICS.EMPTY_SEARCHES_COUNT, counters.emptySearchesCount)
            .set(USER_ANALYTICS.DETAILS_OPENED_COUNT, counters.detailsOpenedCount)
            .set(USER_ANALYTICS.ROUTES_CLICKED_COUNT, counters.routesClickedCount)
            .set(USER_ANALYTICS.LAST_EVENT, event.value)
            .set(USER_ANALYTICS.LAST_EVENT_AT, now)
            .onConflict(USER_ANALYTICS.USER_ID)
            .doUpdate()
            .set(USER_ANALYTICS.SEARCHES_COUNT, USER_ANALYTICS.SEARCHES_COUNT.plus(counters.searchesCount))
            .set(USER_ANALYTICS.SUCCESSFUL_SEARCHES_COUNT, USER_ANALYTICS.SUCCESSFUL_SEARCHES_COUNT.plus(counters.successfulSearchesCount))
            .set(USER_ANALYTICS.EMPTY_SEARCHES_COUNT, USER_ANALYTICS.EMPTY_SEARCHES_COUNT.plus(counters.emptySearchesCount))
            .set(USER_ANALYTICS.DETAILS_OPENED_COUNT, USER_ANALYTICS.DETAILS_OPENED_COUNT.plus(counters.detailsOpenedCount))
            .set(USER_ANALYTICS.ROUTES_CLICKED_COUNT, USER_ANALYTICS.ROUTES_CLICKED_COUNT.plus(counters.routesClickedCount))
            .set(USER_ANALYTICS.LAST_EVENT, event.value)
            .set(USER_ANALYTICS.LAST_EVENT_AT, now)
            .execute()
    }

    private fun insertAnalyticsEvent(
        txCtx: DSLContext,
        userId: UUID?,
        command: EncryptedAnalyticsEventCommand
    ) {
        txCtx
            .insertInto(ANALYTICS_EVENTS)
            .set(ANALYTICS_EVENTS.EVENT, command.event.value)
            .set(ANALYTICS_EVENTS.CREATED_AT, Instant.now())
            .set(ANALYTICS_EVENTS.USER_ID, userId)
            .set(ANALYTICS_EVENTS.SOURCE, command.source.value)
            .set(ANALYTICS_EVENTS.LAT, command.lat)
            .set(ANALYTICS_EVENTS.LON, command.lon)
            .set(ANALYTICS_EVENTS.RESULTS_COUNT, command.resultsCount)
            .set(ANALYTICS_EVENTS.DURATION_MS, command.durationMs)
            .set(ANALYTICS_EVENTS.METADATA, command.metadata.toJSONB())
            .execute()
    }

    private fun ProductAnalyticsEvent.analyticsCounters(): UserAnalyticsCounters =
        when (this) {
            ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED ->
                UserAnalyticsCounters(searchesCount = 1, successfulSearchesCount = 1)

            ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS ->
                UserAnalyticsCounters(searchesCount = 1, emptySearchesCount = 1)

            ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED ->
                UserAnalyticsCounters(detailsOpenedCount = 1)

            ProductAnalyticsEvent.ROUTE_CLICKED ->
                UserAnalyticsCounters(routesClickedCount = 1)
        }

    private data class UserAnalyticsCounters(
        val searchesCount: Int = 0,
        val successfulSearchesCount: Int = 0,
        val emptySearchesCount: Int = 0,
        val detailsOpenedCount: Int = 0,
        val routesClickedCount: Int = 0
    )
}
