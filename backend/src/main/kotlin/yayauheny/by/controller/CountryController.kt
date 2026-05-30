package yayauheny.by.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yayauheny.by.common.config.ApiProperties
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
import yayauheny.by.service.CountryService
import jakarta.validation.Valid
import java.util.UUID

@RestController
@RequestMapping("/api/v1/countries")
class CountryController(
    private val countryService: CountryService,
    private val apiProperties: ApiProperties = ApiProperties()
) {
    @GetMapping
    fun getAllCountries(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(defaultValue = "nameRu") sort: String,
        @RequestParam(defaultValue = "asc") order: String
    ): ResponseEntity<PageResponse<CountryResponseDto>> {
        val pagination = PaginationRequest(
            page = page ?: apiProperties.defaults.page,
            size = size ?: apiProperties.defaults.pageSize,
            sort = sort,
            direction = yayauheny.by.common.query.SortDirection.valueOf(order.uppercase())
        )
        val pageResponse = countryService.getAllCountries(pagination)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/{id}")
    fun getCountryById(@PathVariable id: UUID): ResponseEntity<CountryResponseDto> {
        val country = countryService.getCountryById(id)
            ?: throw NotFoundException("Страна с ID '$id' не найдена")
        return ResponseEntity.ok(country)
    }

    @GetMapping("/code/{code}")
    fun getCountryByCode(@PathVariable code: String): ResponseEntity<CountryResponseDto> {
        val country = countryService.getCountryByCode(code)
            ?: throw NotFoundException("Страна с кодом '$code' не найдена")
        return ResponseEntity.ok(country)
    }

    @PostMapping
    fun createCountry(@Valid @RequestBody createDto: CountryCreateDto): ResponseEntity<CountryResponseDto> {
        val country = countryService.createCountry(createDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(country)
    }

    @PutMapping("/{id}")
    fun updateCountry(
        @PathVariable id: UUID,
        @Valid @RequestBody updateDto: CountryUpdateDto
    ): ResponseEntity<CountryResponseDto> {
        val country = countryService.updateCountry(id, updateDto)
        return ResponseEntity.ok(country)
    }

    @DeleteMapping("/{id}")
    fun deleteCountry(@PathVariable id: UUID): ResponseEntity<Map<String, Boolean>> {
        val deleted = countryService.deleteCountry(id)
        return if (deleted) {
            ResponseEntity.ok(mapOf("deleted" to true))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
