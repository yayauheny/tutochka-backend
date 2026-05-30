package yayauheny.by.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated
import yayauheny.by.common.config.ApiProperties
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.metrics.BackendSearchMetrics
import yayauheny.by.metrics.SearchMetricBuckets
import yayauheny.by.metrics.MetricLabelWhitelist
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.restroom.RestroomFilter
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.service.RestroomService
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.util.toPaginationRequest
import java.util.UUID

@RestController
@RequestMapping("/api/v1/restrooms")
@Validated
class RestroomController(
    private val restroomService: RestroomService,
    private val backendSearchMetrics: BackendSearchMetrics,
    private val analyticsService: AnalyticsService,
    private val apiProperties: ApiProperties
) {
    private val log = LoggerFactory.getLogger(RestroomController::class.java)

    @GetMapping
    fun getAllRestrooms(
        @ModelAttribute filter: RestroomFilter,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val pagination = request.toPaginationRequest(
            defaultPage = apiProperties.defaults.page,
            defaultSize = apiProperties.defaults.pageSize,
            maxSize = apiProperties.limits.maxPageSize
        )
        val pageResponse = restroomService.getAllRestrooms(filter, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/{id}")
    fun getRestroomById(@PathVariable id: UUID): ResponseEntity<Any> {
        val restroom = restroomService.getRestroomById(id)
            ?: throw EntityNotFoundException("Туалет с ID '$id' не найден")
        return ResponseEntity.ok(restroom)
    }

    @GetMapping("/city/{cityId}")
    fun getRestroomsByCity(
        @PathVariable cityId: UUID,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val pagination = request.toPaginationRequest(
            defaultPage = apiProperties.defaults.page,
            defaultSize = apiProperties.defaults.pageSize,
            maxSize = apiProperties.limits.maxPageSize
        )
        val pageResponse = restroomService.getRestroomsByCity(cityId, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/nearest")
    fun getNearestRestrooms(
        @RequestHeader(name = "X-Client-Type", required = false) clientTypeHeader: String?,
        @RequestHeader(name = "X-Tg-User-Id", required = false) tgUserId: Long?,
        @RequestHeader(name = "X-Tg-Chat-Id", required = false) tgChatId: String?,
        @RequestHeader(name = "X-Tg-Username", required = false) username: String?,
        @RequestHeader(name = "X-Analytics-Source", required = false) sourceHeader: String?,
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(required = false) @Min(1) @Max(100) limit: Int?,
        @RequestParam(required = false) @Min(1) @Max(10000) distanceMeters: Int?
    ): ResponseEntity<Any> {
        val resolvedLimit = limit ?: apiProperties.defaults.maxNearestRestroomsSize
        val resolvedDistanceMeters = distanceMeters ?: apiProperties.defaults.maxDistanceMeters
        val clientType = MetricLabelWhitelist.clientTypeOrDefault(clientTypeHeader)
        val radiusBucket = SearchMetricBuckets.radiusBucketOrFallback(resolvedDistanceMeters.toString())
        backendSearchMetrics.incrementSearchRequest(clientType, radiusBucket)

        try {
            val restrooms = restroomService.findNearestRestrooms(
                lat,
                lon,
                resolvedLimit,
                resolvedDistanceMeters
            )

            val resultsCount = restrooms.size
            val firstDistanceMeters = restrooms.firstOrNull()?.distanceMeters?.toInt()

            backendSearchMetrics.incrementSearchResults(
                clientType = clientType,
                resultBucket = SearchMetricBuckets.resultBucket(resultsCount)
            )
            backendSearchMetrics.incrementSearchQuality(
                clientType = clientType,
                qualityBucket = SearchMetricBuckets.qualityBucket(resultsCount, firstDistanceMeters)
            )

            val source = sourceHeader?.let { AnalyticsSource.fromValue(it) } ?: AnalyticsSource.TELEGRAM_BOT
            val identity = AnalyticsIdentityHeaders(
                tgUserId = tgUserId,
                tgChatId = tgChatId,
                username = username,
                source = source
            )
            runCatching {
                analyticsService.trackNearestRestroomsSearch(identity, resultsCount, lat, lon)
            }.onFailure { exception ->
                log.warn("Failed to track nearest restroom analytics", exception)
            }

            return ResponseEntity.ok(restrooms)
        } catch (e: ValidationException) {
            backendSearchMetrics.incrementSearchFailure("validation")
            throw e
        } catch (e: IllegalArgumentException) {
            backendSearchMetrics.incrementSearchFailure("validation")
            throw e
        } catch (e: EntityNotFoundException) {
            backendSearchMetrics.incrementSearchFailure("not_found")
            throw e
        } catch (e: Exception) {
            backendSearchMetrics.incrementSearchFailure("internal")
            throw e
        }
    }

    @PostMapping
    fun createRestroom(@Valid @RequestBody createDto: RestroomCreateDto): ResponseEntity<Any> {
        val restroom = restroomService.createRestroom(createDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(restroom)
    }

    @PutMapping("/{id}")
    fun updateRestroom(@PathVariable id: UUID, @Valid @RequestBody updateDto: RestroomUpdateDto): ResponseEntity<Any> {
        val restroom = restroomService.updateRestroom(id, updateDto)
        return ResponseEntity.ok(restroom)
    }

    @DeleteMapping("/{id}")
    fun deleteRestroom(@PathVariable id: UUID): ResponseEntity<Any> {
        val deleted = restroomService.deleteRestroom(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("deleted" to true))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
