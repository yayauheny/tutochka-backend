package yayauheny.by.unit.di

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import yayauheny.by.service.import.schedule.OsmScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.schedule.YandexScheduleAdapter

@SpringJUnitConfig(classes = [ServiceModuleTest.Config::class])
class ServiceModuleTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun given_application_context_when_resolving_schedule_mapping_dependencies_then_spring_provides_them() {
        val scheduleMappingService = applicationContext.getBean(ScheduleMappingService::class.java)
        val adapters = applicationContext.getBeansOfType(ScheduleAdapter::class.java).values.toList()

        assertThat(scheduleMappingService).isNotNull()
        assertThat(adapters).hasSize(3)
        assertThat(adapters).anyMatch { it is TwoGisScheduleAdapter }
        assertThat(adapters).anyMatch { it is YandexScheduleAdapter }
        assertThat(adapters).anyMatch { it is OsmScheduleAdapter }
    }

    @TestConfiguration
    class Config {
        @Bean
        fun twoGisScheduleAdapter(): TwoGisScheduleAdapter = TwoGisScheduleAdapter()

        @Bean
        fun yandexScheduleAdapter(): YandexScheduleAdapter = YandexScheduleAdapter()

        @Bean
        fun osmScheduleAdapter(): OsmScheduleAdapter = OsmScheduleAdapter()

        @Bean
        fun scheduleMappingService(adapters: List<ScheduleAdapter>): ScheduleMappingService = ScheduleMappingService(adapters)
    }
}
