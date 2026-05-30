package yayauheny.by.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yayauheny.by.common.config.ApiProperties
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.model.city.CityFilter
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.service.CityService
import yayauheny.by.util.toPaginationRequest
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cities")
class CityController(
    private val cityService: CityService,
    private val apiProperties: ApiProperties
) {
    @GetMapping
    fun getAllCities(
        @ModelAttribute filter: CityFilter,
        request: HttpServletRequest
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val pagination = request.toPaginationRequest(
            defaultPage = apiProperties.defaults.page,
            defaultSize = apiProperties.defaults.pageSize,
            maxSize = apiProperties.limits.maxPageSize
        )
        val pageResponse = cityService.getAllCities(filter, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/{id}")
    fun getCityById(@PathVariable id: UUID): ResponseEntity<CityResponseDto> {
        val city = cityService.getCityById(id)
            ?: throw NotFoundException("Город с ID '$id' не найден")
        return ResponseEntity.ok(city)
    }

    @GetMapping("/country/{countryId}")
    fun getCitiesByCountry(
        @PathVariable countryId: UUID,
        request: HttpServletRequest
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val pagination = request.toPaginationRequest(
            defaultPage = apiProperties.defaults.page,
            defaultSize = apiProperties.defaults.pageSize,
            maxSize = apiProperties.limits.maxPageSize
        )
        val pageResponse = cityService.getCitiesByCountry(countryId, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/search")
    fun searchCitiesByName(
        @RequestParam(required = false, defaultValue = "") @Size(min = 1, max = 100) name: String,
        request: HttpServletRequest
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val validName = name.trim()
        val pagination = request.toPaginationRequest(
            defaultPage = apiProperties.defaults.page,
            defaultSize = apiProperties.defaults.pageSize,
            maxSize = apiProperties.limits.maxPageSize
        )
        val pageResponse = cityService.searchCitiesByName(validName, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @PostMapping
    fun createCity(@Valid @RequestBody createDto: CityCreateDto): ResponseEntity<CityResponseDto> {
        val city = cityService.createCity(createDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(city)
    }

    @PutMapping("/{id}")
    fun updateCity(
        @PathVariable id: UUID,
        @Valid @RequestBody updateDto: CityUpdateDto
    ): ResponseEntity<CityResponseDto> {
        val city = cityService.updateCity(id, updateDto)
        return ResponseEntity.ok(city)
    }

    @DeleteMapping("/{id}")
    fun deleteCity(@PathVariable id: UUID): ResponseEntity<Map<String, Boolean>> {
        val deleted = cityService.deleteCity(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("deleted" to true))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
