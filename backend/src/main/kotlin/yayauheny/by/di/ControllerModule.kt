package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.dsl.module
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.HealthController
import yayauheny.by.controller.ImportController
import yayauheny.by.controller.RestroomController
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService
import yayauheny.by.repository.CityRepository
import yayauheny.by.service.import.ImportService

val controllerModule =
    module {
        single<CountryController> { CountryController(get<CountryService>()) }
        single<CityController> { CityController(get<CityService>()) }
        single<RestroomController> { RestroomController(get<RestroomService>()) }
        single<HealthController> { HealthController(get<DSLContext>()) }
        single<ImportController> { ImportController(get<ImportService>(), get<CityRepository>()) }
    }
