package yayauheny.by.di

import org.koin.dsl.module
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService

val serviceModule =
    module {
        single<CountryService> { CountryService(get()) }
        single<CityService> { CityService(get(), get()) }
        single<RestroomService> { RestroomService(get()) }
    }
