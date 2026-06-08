package yayauheny.by.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated
import yayauheny.by.common.config.ApiProperties
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.restroom.RestroomFilter
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.service.RestroomService
import java.util.UUID

@RestController
@RequestMapping("/api/v1/restrooms")
@Validated
class RestroomController(
    private val restroomService: RestroomService,
    private val businessAnalyticsPublisher: BusinessAnalyticsPublisher,
    private val apiProperties: ApiProperties
) {
    @GetMapping
    fun getAllRestrooms(
        @Valid @ModelAttribute filter: RestroomFilter,
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
        @RequestHeader(name = "X-Tg-User-Id", required = false) tgUserId: Long?,
        @RequestHeader(name = "X-Tg-Chat-Id", required = false) tgChatId: String?,
        @RequestHeader(name = "X-Tg-Username", required = false) username: String?,
        @RequestHeader(name = "X-Analytics-Source", required = false) sourceHeader: String?,
        @RequestParam @DecimalMin(value = "-90.0", message = "должно быть не менее -90 градусов") @DecimalMax(value = "90.0", message = "не более 90 градусов") lat: Double,
        @RequestParam @DecimalMin(value = "-180.0", message = "должно быть не менее -180 градусов") @DecimalMax(value = "180.0", message = "не более 180 градусов") lon: Double,
        @RequestParam(required = false) @Min(1) @Max(100) limit: Int?,
        @RequestParam(required = false) @Min(1) @Max(20000) distanceMeters: Int?
    ): ResponseEntity<Any> {
        val resolvedLimit = limit ?: apiProperties.defaults.maxNearestRestroomsSize
        val resolvedDistanceMeters = distanceMeters ?: apiProperties.defaults.maxDistanceMeters
        val normalizedUsername = username?.trim()?.takeIf { it.isNotEmpty() }

        try {
            val restrooms = restroomService.findNearestRestrooms(
                lat,
                lon,
                resolvedLimit,
                resolvedDistanceMeters
            )

            val resultsCount = restrooms.size
            val source = resolveAnalyticsSource(sourceHeader, tgUserId, tgChatId)
            businessAnalyticsPublisher.trackNearestRestroomsSearch(
                identity = AnalyticsIdentityHeaders(
                    tgUserId = tgUserId,
                    tgChatId = tgChatId,
                    username = normalizedUsername,
                    source = source
                ),
                resultsCount = resultsCount,
                lat = lat,
                lon = lon
            )

            return ResponseEntity.ok(restrooms)
        } catch (e: ValidationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: EntityNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createRestroom(@Valid @RequestBody createDto: RestroomCreateDto): ResponseEntity<Any> {
        val restroom = restroomService.createRestroom(createDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(restroom)
    }

    @PutMapping(value = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
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

    private fun resolveAnalyticsSource(
        sourceHeader: String?,
        tgUserId: Long?,
        tgChatId: String?
    ): AnalyticsSource {
        return sourceHeader?.let(AnalyticsSource::fromValue)
            ?: if (tgUserId != null && !tgChatId.isNullOrBlank()) {
                AnalyticsSource.TELEGRAM_BOT
            } else {
                AnalyticsSource.API
            }
    }

    private fun HttpServletRequest.toPaginationRequest(
        defaultPage: Int,
        defaultSize: Int,
        maxSize: Int
    ): PaginationRequest {
        val page = getParameter("page")?.toIntOrNull()?.coerceAtLeast(0) ?: defaultPage.coerceAtLeast(0)
        val parsedSize = getParameter("size")?.toIntOrNull() ?: defaultSize
        val size = parsedSize.coerceAtLeast(1).coerceAtMost(maxSize.coerceAtLeast(1))
        val sort = getParameter("sort")?.trim()?.takeIf { it.isNotEmpty() }
        val direction =
            if (getParameter("direction")?.trim()?.uppercase() == SortDirection.DESC.name) {
                SortDirection.DESC
            } else {
                SortDirection.ASC
            }

        return PaginationRequest(
            sort = sort,
            direction = direction,
            page = page,
            size = size
        )
    }
}
