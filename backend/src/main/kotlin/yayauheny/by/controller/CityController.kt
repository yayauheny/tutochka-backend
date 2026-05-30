package yayauheny.by.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.service.CityService
import yayauheny.by.service.validation.CitySearchParams
import yayauheny.by.service.validation.validateAndThen
import yayauheny.by.service.validation.validateCityOnCreate
import yayauheny.by.service.validation.validateCityOnUpdate
import yayauheny.by.service.validation.validateCitySearchParams
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.service.validation.validateRegion
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cities")
class CityController(
    private val cityService: CityService
) {
    @GetMapping
    fun getAllCities(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val pagination = PaginationRequest(page = page, size = size)
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
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val pagination = PaginationRequest(page = page, size = size)
        val pageResponse = cityService.getCitiesByCountry(countryId, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/search")
    fun searchCitiesByName(
        @RequestParam(required = false, defaultValue = "") name: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<CityResponseDto>> {
        val searchParams = CitySearchParams(name = name.trim())
        val validParams = searchParams.validateAndThen(validateCitySearchParams) { it }.getOrThrow()

        val pagination = PaginationRequest(page = page, size = size)
        val pageResponse = cityService.searchCitiesByName(validParams.name, pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @PostMapping
    fun createCity(@RequestBody createDto: CityCreateDto): ResponseEntity<CityResponseDto> {
        createDto.validateOrThrow(validateCityOnCreate)
        val additionalErrors = validateRegion(createDto.region)
        if (additionalErrors.isNotEmpty()) {
            throw ValidationException(additionalErrors)
        }
        val city = cityService.createCity(createDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(city)
    }

    @PutMapping("/{id}")
    fun updateCity(
        @PathVariable id: UUID,
        @RequestBody updateDto: CityUpdateDto
    ): ResponseEntity<CityResponseDto> {
        updateDto.validateOrThrow(validateCityOnUpdate)
        val additionalErrors = validateRegion(updateDto.region)
        if (additionalErrors.isNotEmpty()) {
            throw ValidationException(additionalErrors)
        }
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
