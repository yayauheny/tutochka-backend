package yayauheny.by.analytics.repository

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsEventCount
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsMonthlyAggregate
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.tables.references.ANALYTICS_EVENTS
import yayauheny.by.tables.references.USERS

class PublicAnalyticsRepositoryImpl(
    private val ctx: DSLContext
) : PublicAnalyticsRepository {
    override suspend fun countTotalUsers(): Long =
        withContext(Dispatchers.IO) {
            ctx.fetchCount(USERS).toLong()
        }

    override suspend fun countNewUsers(window: AnalyticsWindow): Long =
        withContext(Dispatchers.IO) {
            ctx
                .fetchCount(
                    USERS,
                    USERS.CREATED_AT.ge(window.start).and(USERS.CREATED_AT.lt(window.end))
                ).toLong()
        }

    override suspend fun countActiveUsers(window: AnalyticsWindow): Long =
        withContext(Dispatchers.IO) {
            ctx
                .selectCount()
                .from(
                    ctx
                        .selectDistinct(ANALYTICS_EVENTS.USER_ID)
                        .from(ANALYTICS_EVENTS)
                        .where(
                            ANALYTICS_EVENTS.USER_ID.isNotNull
                                .and(ANALYTICS_EVENTS.CREATED_AT.ge(window.start))
                                .and(ANALYTICS_EVENTS.CREATED_AT.lt(window.end))
                        )
                ).fetchOne(0, Long::class.java)
                ?: 0L
        }

    override suspend fun aggregateEventMetrics(window: AnalyticsWindow?): PublicAnalyticsEventMetrics =
        withContext(Dispatchers.IO) {
            val searchCondition = searchEventCondition()
            val successfulSearchesField =
                sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value), "successful_searches")
            val emptySearchesField =
                sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS.value), "empty_searches")
            val totalSearchesField = sumCase(searchCondition, "total_searches")
            val totalResultsField = sumSearchResults(searchCondition, "total_results")
            val routeClicksField = sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.ROUTE_CLICKED.value), "route_clicks")
            val detailsOpenedField =
                sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value), "details_opened")

            val record =
                ctx
                    .select(
                        totalSearchesField,
                        successfulSearchesField,
                        emptySearchesField,
                        totalResultsField,
                        routeClicksField,
                        detailsOpenedField
                    ).from(ANALYTICS_EVENTS)
                    .where(analyticsWindowCondition(window))
                    .fetchOne()

            PublicAnalyticsEventMetrics(
                totalSearches = record?.get(totalSearchesField) ?: 0L,
                successfulSearches = record?.get(successfulSearchesField) ?: 0L,
                emptySearches = record?.get(emptySearchesField) ?: 0L,
                totalResultsAcrossSearches = record?.get(totalResultsField) ?: 0L,
                routeClicks = record?.get(routeClicksField) ?: 0L,
                detailsOpened = record?.get(detailsOpenedField) ?: 0L
            )
        }

    override suspend fun fetchDailySeries(
        window: AnalyticsWindow,
        zoneId: ZoneId
    ): List<PublicAnalyticsDailyAggregate> =
        withContext(Dispatchers.IO) {
            val dayField = dayBucketField(ANALYTICS_EVENTS.CREATED_AT, zoneId, "day")
            val searchCondition = searchEventCondition()
            val activeUsersField = DSL.countDistinct(ANALYTICS_EVENTS.USER_ID).cast(SQLDataType.BIGINT).`as`("active_users")
            val searchesField = sumCase(searchCondition, "searches")
            val successfulSearchesField =
                sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value), "successful_searches")
            val emptySearchesField =
                sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS.value), "empty_searches")
            val routeClicksField = sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.ROUTE_CLICKED.value), "route_clicks")
            val detailsOpenedField =
                sumCase(ANALYTICS_EVENTS.EVENT.eq(ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value), "details_opened")

            ctx
                .select(
                    dayField,
                    activeUsersField,
                    searchesField,
                    successfulSearchesField,
                    emptySearchesField,
                    routeClicksField,
                    detailsOpenedField
                ).from(ANALYTICS_EVENTS)
                .where(analyticsWindowCondition(window))
                .groupBy(dayField)
                .orderBy(dayField.asc())
                .fetch()
                .map { record ->
                    PublicAnalyticsDailyAggregate(
                        date = requireNotNull(record.get(dayField)).toString(),
                        activeUsers = record.get(activeUsersField) ?: 0L,
                        searches = record.get(searchesField) ?: 0L,
                        successfulSearches = record.get(successfulSearchesField) ?: 0L,
                        emptySearches = record.get(emptySearchesField) ?: 0L,
                        routeClicks = record.get(routeClicksField) ?: 0L,
                        detailsOpened = record.get(detailsOpenedField) ?: 0L
                    )
                }
        }

    override suspend fun fetchMonthlySeries(
        startMonth: YearMonth,
        monthsCount: Int,
        zoneId: ZoneId
    ): List<PublicAnalyticsMonthlyAggregate> =
        withContext(Dispatchers.IO) {
            val start = monthStartInstant(startMonth, zoneId)
            val end = monthStartInstant(startMonth.plusMonths(monthsCount.toLong()), zoneId)
            val eventRows = fetchMonthlyEventRows(start, end, zoneId)
            val newUsersRows = fetchMonthlyNewUsers(start, end, zoneId)

            val months =
                (0 until monthsCount)
                    .associate { offset ->
                        val month = startMonth.plusMonths(offset.toLong()).toString()
                        month to PublicAnalyticsMonthlyAggregate(month = month, newUsers = 0, activeUsers = 0, searches = 0)
                    }.toMutableMap()

            newUsersRows.forEach { row ->
                months[row.month] = months.getValue(row.month).copy(newUsers = row.newUsers)
            }
            eventRows.forEach { row ->
                months[row.month] =
                    months.getValue(row.month).copy(
                        activeUsers = row.activeUsers,
                        searches = row.searches
                    )
            }

            months.values.toList()
        }

    override suspend fun fetchEventTotals(minimumCount: Long): List<PublicAnalyticsEventCount> =
        withContext(Dispatchers.IO) {
            ctx
                .select(ANALYTICS_EVENTS.EVENT, DSL.count().cast(SQLDataType.BIGINT).`as`("event_count"))
                .from(ANALYTICS_EVENTS)
                .groupBy(ANALYTICS_EVENTS.EVENT)
                .having(DSL.count().ge(minimumCount.toInt()))
                .orderBy(DSL.field("event_count").desc())
                .fetch()
                .map { record ->
                    PublicAnalyticsEventCount(
                        eventKey = requireNotNull(record.get(ANALYTICS_EVENTS.EVENT)),
                        count = record.get("event_count", Long::class.java) ?: 0L
                    )
                }
        }

    private fun fetchMonthlyEventRows(
        start: Instant,
        end: Instant,
        zoneId: ZoneId
    ): List<MonthlyEventRow> {
        val monthField = monthBucketField(ANALYTICS_EVENTS.CREATED_AT, zoneId, "month")
        val activeUsersField = DSL.countDistinct(ANALYTICS_EVENTS.USER_ID).cast(SQLDataType.BIGINT).`as`("active_users")
        val searchesField = sumCase(searchEventCondition(), "searches")

        return ctx
            .select(monthField, activeUsersField, searchesField)
            .from(ANALYTICS_EVENTS)
            .where(ANALYTICS_EVENTS.CREATED_AT.ge(start).and(ANALYTICS_EVENTS.CREATED_AT.lt(end)))
            .groupBy(monthField)
            .orderBy(monthField.asc())
            .fetch()
            .map { record ->
                MonthlyEventRow(
                    month = requireNotNull(record.get(monthField)),
                    activeUsers = record.get(activeUsersField) ?: 0L,
                    searches = record.get(searchesField) ?: 0L
                )
            }
    }

    private fun fetchMonthlyNewUsers(
        start: Instant,
        end: Instant,
        zoneId: ZoneId
    ): List<MonthlyNewUsersRow> {
        val monthField = monthBucketField(USERS.CREATED_AT, zoneId, "month")
        val newUsersField = DSL.count().cast(SQLDataType.BIGINT).`as`("new_users")

        return ctx
            .select(monthField, newUsersField)
            .from(USERS)
            .where(USERS.CREATED_AT.ge(start).and(USERS.CREATED_AT.lt(end)))
            .groupBy(monthField)
            .orderBy(monthField.asc())
            .fetch()
            .map { record ->
                MonthlyNewUsersRow(
                    month = requireNotNull(record.get(monthField)),
                    newUsers = record.get(newUsersField) ?: 0L
                )
            }
    }

    private fun analyticsWindowCondition(window: AnalyticsWindow?): Condition {
        if (window == null) {
            return DSL.trueCondition()
        }
        return ANALYTICS_EVENTS.CREATED_AT.ge(window.start).and(ANALYTICS_EVENTS.CREATED_AT.lt(window.end))
    }

    private fun searchEventCondition(): Condition =
        ANALYTICS_EVENTS.EVENT.`in`(
            ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value,
            ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS.value
        )

    private fun sumCase(
        condition: Condition,
        alias: String
    ): Field<Long> =
        DSL
            .coalesce(
                DSL
                    .sum(
                        DSL.`when`(condition, 1).otherwise(0)
                    ).cast(SQLDataType.BIGINT),
                DSL.inline(0L)
            ).`as`(alias)

    private fun sumSearchResults(
        condition: Condition,
        alias: String
    ): Field<Long> =
        DSL
            .coalesce(
                DSL
                    .sum(
                        DSL.`when`(condition, DSL.coalesce(ANALYTICS_EVENTS.RESULTS_COUNT, 0)).otherwise(0)
                    ).cast(SQLDataType.BIGINT),
                DSL.inline(0L)
            ).`as`(alias)

    private fun dayBucketField(
        field: Field<Instant?>,
        zoneId: ZoneId,
        alias: String
    ): Field<java.time.LocalDate> =
        DSL
            .field(
                "date(timezone({0}, {1}))",
                SQLDataType.LOCALDATE,
                DSL.inline(zoneId.id),
                field
            ).`as`(alias)

    private fun monthBucketField(
        field: Field<Instant?>,
        zoneId: ZoneId,
        alias: String
    ): Field<String> =
        DSL
            .field(
                "to_char(date_trunc('month', timezone({0}, {1})), 'YYYY-MM')",
                SQLDataType.VARCHAR(7),
                DSL.inline(zoneId.id),
                field
            ).`as`(alias)

    private fun monthStartInstant(
        month: YearMonth,
        zoneId: ZoneId
    ): Instant = month.atDay(1).atStartOfDay(zoneId).toInstant()

    private data class MonthlyNewUsersRow(
        val month: String,
        val newUsers: Long
    )

    private data class MonthlyEventRow(
        val month: String,
        val activeUsers: Long,
        val searches: Long
    )
}
