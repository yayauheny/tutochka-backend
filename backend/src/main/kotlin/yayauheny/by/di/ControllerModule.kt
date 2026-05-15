package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.dsl.module
import yayauheny.by.analytics.api.AnalyticsController
import yayauheny.by.analytics.api.PublicAnalyticsController
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.service.PublicAnalyticsService
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.HealthController
import yayauheny.by.controller.RestroomController
import yayauheny.by.metrics.BackendSearchMetrics
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService

val controllerModule =
    module {
        single<CountryController> { CountryController(get<CountryService>()) }
        single<CityController> { CityController(get<CityService>()) }
        single<RestroomController> { RestroomController(get<RestroomService>(), get<BackendSearchMetrics>(), get<AnalyticsService>()) }
        single<HealthController> { HealthController(get<DSLContext>()) }
        single<AnalyticsController> { AnalyticsController(get<AnalyticsService>()) }
        single<PublicAnalyticsController> { PublicAnalyticsController(get<PublicAnalyticsService>()) }
    }
