package yayauheny.by.repository

import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
import java.util.UUID

interface CountryRepository : BaseRepository<CountryResponseDto, CountryCreateDto, CountryUpdateDto, UUID>
