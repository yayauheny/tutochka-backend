package yayauheny.by.unit.di

import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import yayauheny.by.di.serviceModule
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ServiceModuleTest {
    @Test
    fun given_service_module_when_resolving_schedule_mapping_dependencies_then_koin_provides_them() {
        val app =
            koinApplication {
                modules(serviceModule)
            }

        try {
            val scheduleMappingService = app.koin.get<ScheduleMappingService>()
            val adapters = app.koin.getAll<ScheduleAdapter>()

            assertIs<ScheduleMappingService>(scheduleMappingService)
            assertEquals(1, adapters.size)
            assertIs<TwoGisScheduleAdapter>(adapters.single())
        } finally {
            app.close()
        }
    }
}
