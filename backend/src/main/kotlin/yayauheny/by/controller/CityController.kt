package yayauheny.by.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yayauheny.by.common.config.ApiProperties
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.service.CityService
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cities")
class CityController(
    private val cityService: CityService,
    private val apiProperties: ApiProperties = ApiProperties()
) {
    @GetMapping
    fun getAllCities(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val pagination = PaginationRequest(
            page = page ?: apiProperties.defaults.page,
            size = size ?: apiProperties.defaults.pageSize
        )
        val pageResponse = cityService.getAllCities(pagination)
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
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val pagination = PaginationRequest(
            page = page ?: apiProperties.defaults.page,
            size = size ?: apiProperties.defaults.pageSize
        )
        val pageResponse = cityService.getCitiesByCountry(countryId, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/search")
    fun searchCitiesByName(
        @RequestParam(required = false, defaultValue = "") @Size(min = 1, max = 100) name: String,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val validName = name.trim()

        val pagination = PaginationRequest(
            page = page ?: apiProperties.defaults.page,
            size = size ?: apiProperties.defaults.pageSize
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
