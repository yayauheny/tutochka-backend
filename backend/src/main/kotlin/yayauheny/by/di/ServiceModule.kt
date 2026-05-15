package yayauheny.by.di

import org.koin.dsl.module
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter

val serviceModule =
    module {
        single<ScheduleAdapter> { TwoGisScheduleAdapter() }
        single { ScheduleMappingService(getAll()) }
        single<CountryService> { CountryService(get()) }
        single<CityService> { CityService(get(), get()) }
        single<RestroomService> { RestroomService(get()) }
    }
