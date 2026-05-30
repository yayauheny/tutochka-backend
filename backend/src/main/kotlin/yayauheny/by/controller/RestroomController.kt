package yayauheny.by.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.metrics.BackendSearchMetrics
import yayauheny.by.metrics.SearchMetricBuckets
import yayauheny.by.metrics.MetricLabelWhitelist
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.service.RestroomService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import java.util.UUID

@RestController
@RequestMapping("/restrooms")
@Validated
class RestroomController(
    private val restroomService: RestroomService,
    private val backendSearchMetrics: BackendSearchMetrics,
    private val analyticsService: AnalyticsService
) {
    private val log = LoggerFactory.getLogger(RestroomController::class.java)

    @GetMapping
    fun getAllRestrooms(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sort: String,
        @RequestParam(defaultValue = "desc") order: String
    ): ResponseEntity<Any> {
        val pagination = PaginationRequest(page = page, size = size, sort = sort, direction = yayauheny.by.common.query.SortDirection.valueOf(order.uppercase()))
        val pageResponse = restroomService.getAllRestrooms(pagination)
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
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sort: String,
        @RequestParam(defaultValue = "desc") order: String
    ): ResponseEntity<Any> {
        val pagination = PaginationRequest(page = page, size = size, sort = sort, direction = yayauheny.by.common.query.SortDirection.valueOf(order.uppercase()))
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
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) limit: Int,
        @RequestParam(defaultValue = "5000") @Min(1) @Max(10000) distanceMeters: Int
    ): ResponseEntity<Any> {
        val clientType = MetricLabelWhitelist.clientTypeOrDefault(clientTypeHeader)
        val radiusBucket = SearchMetricBuckets.radiusBucketOrFallback(distanceMeters.toString())
        backendSearchMetrics.incrementSearchRequest(clientType, radiusBucket)

        try {
            val restrooms = restroomService.findNearestRestrooms(
                lat,
                lon,
                limit,
                distanceMeters
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
