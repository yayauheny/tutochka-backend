package yayauheny.by.di

import org.koin.dsl.module
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.RestroomController
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService

val controllerModule = module {
    single<CountryController> { CountryController(get<CountryService>()) }
    single<CityController> { CityController(get<CityService>()) }
    single<RestroomController> { RestroomController(get<RestroomService>()) }
}
