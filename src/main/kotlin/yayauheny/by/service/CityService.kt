package yayauheny.by.service

import java.util.UUID
import org.slf4j.LoggerFactory
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import yayauheny.by.service.validation.Validated
import yayauheny.by.service.validation.cityCreateValidator
import yayauheny.by.service.validation.validateWith

class CityService(
    private val cityRepository: CityRepository,
    private val countryRepository: CountryRepository
) {
    private val logger = LoggerFactory.getLogger(CityService::class.java)

    suspend fun getAllCities(pagination: PaginationRequest): PageResponse<CityResponseDto> = cityRepository.findAll(pagination)

    suspend fun getCityById(id: UUID): CityResponseDto? = cityRepository.findById(id)

    suspend fun getCitiesByCountry(
        countryId: UUID,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> = cityRepository.findByCountryId(countryId, pagination)

    suspend fun searchCitiesByName(
        name: String,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> = cityRepository.findByName(name, pagination)

    suspend fun createCity(createDto: CityCreateDto): CityResponseDto {
        logger.info("createCity() called: countryId=${createDto.countryId}, nameRu=${createDto.nameRu}, nameEn=${createDto.nameEn}")

        try {
            logger.info("Validating CityCreateDto")
            val validationResult = createDto.validateWith(cityCreateValidator)

            // Валидация nullable полей на уровне сервиса для единообразия
            val validationErrors = mutableListOf<yayauheny.by.common.errors.FieldError>()

            if (validationResult is Validated.Fail<CityCreateDto>) {
                validationErrors.addAll(validationResult.errors)
            }

            createDto.region?.let { region ->
                if (region.length < 2 || region.length > 255) {
                    validationErrors.add(
                        yayauheny.by.common.errors.FieldError(
                            "region",
                            if (region.length < 2) {
                                "Регион должен содержать минимум 2 символа"
                            } else {
                                "Регион слишком длинный (максимум 255 символов)"
                            }
                        )
                    )
                }
            }

            if (validationErrors.isNotEmpty()) {
                throw yayauheny.by.common.errors
                    .ValidationException(errors = validationErrors)
            }

            logger.info("Validation passed")

            logger.info("Checking if country exists: ${createDto.countryId}")
            countryRepository.findById(createDto.countryId)
                ?: throw IllegalArgumentException("Страна с ID '${createDto.countryId}' не найдена")
            logger.info("Country found")

            logger.info("Checking for duplicate by nameRu: ${createDto.nameRu}")
            val existingByRu =
                cityRepository.findSingle(
                    listOf(
                        FilterCriteria("countryId", FilterOperator.EQ, createDto.countryId.toString()),
                        FilterCriteria("nameRu", FilterOperator.EQ, createDto.nameRu)
                    )
                )
            if (existingByRu != null) {
                logger.warn("Duplicate city found by nameRu: ${createDto.nameRu}")
                throw yayauheny.by.common.errors
                    .ConflictException("Город с названием '${createDto.nameRu}' уже существует в этой стране")
            }
            logger.info("No duplicate by nameRu found")

            logger.info("Checking for duplicate by nameEn: ${createDto.nameEn}")
            val existingByEn =
                cityRepository.findSingle(
                    listOf(
                        FilterCriteria("countryId", FilterOperator.EQ, createDto.countryId.toString()),
                        FilterCriteria("nameEn", FilterOperator.EQ, createDto.nameEn)
                    )
                )
            if (existingByEn != null) {
                logger.warn("Duplicate city found by nameEn: ${createDto.nameEn}")
                throw yayauheny.by.common.errors
                    .ConflictException("Город с названием '${createDto.nameEn}' уже существует в этой стране")
            }
            logger.info("No duplicate by nameEn found")

            logger.info("Calling cityRepository.save()")
            val result = cityRepository.save(createDto)
            logger.info("City saved successfully: id=${result.id}, nameRu=${result.nameRu}, nameEn=${result.nameEn}")
            return result
        } catch (e: Exception) {
            logger.error("Error in createCity()", e)
            throw e
        }
    }

    suspend fun updateCity(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto = cityRepository.update(id, updateDto)

    suspend fun deleteCity(id: UUID): Boolean = cityRepository.deleteById(id)
}
