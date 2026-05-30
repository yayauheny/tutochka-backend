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
import yayauheny.by.metrics.extractClientType
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.service.RestroomService
import yayauheny.by.service.validation.*
import yayauheny.by.analytics.api.getAnalyticsIdentityHeaders
import java.util.UUID

@RestController
@RequestMapping("/restrooms")
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
        @RequestHeader headers: HttpHeaders,
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "5000") distanceMeters: Int
    ): ResponseEntity<Any> {
        val clientType = headers.extractClientType()
        val radiusBucket = SearchMetricBuckets.radiusBucketOrFallback(distanceMeters.toString())
        backendSearchMetrics.incrementSearchRequest(clientType, radiusBucket)

        try {
            val params = NearestRestroomsParams(Coordinates(lat, lon), limit, distanceMeters)

            val restrooms = params.validateAndThen(validateNearestRestroomsParams) { valid ->
                restroomService.findNearestRestrooms(
                    valid.coordinates.lat,
                    valid.coordinates.lon,
                    valid.limit,
                    valid.distanceMeters
                )
            }.getOrThrow()

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

            val identity = headers.getAnalyticsIdentityHeaders(AnalyticsSource.TELEGRAM_BOT)
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
    fun createRestroom(@RequestBody createDto: RestroomCreateDto): ResponseEntity<Any> {
        val additionalErrors = validateRestroomCreateFields(createDto)
        if (additionalErrors.isNotEmpty()) {
            throw ValidationException(additionalErrors)
        }
        val restroom = restroomService.createRestroom(createDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(restroom)
    }

    @PutMapping("/{id}")
    fun updateRestroom(@PathVariable id: UUID, @RequestBody updateDto: RestroomUpdateDto): ResponseEntity<Any> {
        val additionalErrors = validateRestroomUpdateFields(updateDto)
        if (additionalErrors.isNotEmpty()) {
            throw ValidationException(additionalErrors)
        }
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
