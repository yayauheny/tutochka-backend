package yayauheny.by.unit.di

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter

@SpringJUnitConfig(classes = [ServiceModuleTest.Config::class])
class ServiceModuleTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun given_application_context_when_resolving_schedule_mapping_dependencies_then_spring_provides_them() {
        val scheduleMappingService = applicationContext.getBean(ScheduleMappingService::class.java)
        val adapters = applicationContext.getBeansOfType(ScheduleAdapter::class.java).values.toList()

        assertThat(scheduleMappingService).isNotNull()
        assertThat(adapters).hasSize(1)
        assertThat(adapters.single()).isInstanceOf(TwoGisScheduleAdapter::class.java)
    }

    @TestConfiguration
    class Config {
        @Bean
        fun twoGisScheduleAdapter(): TwoGisScheduleAdapter = TwoGisScheduleAdapter()

        @Bean
        fun scheduleMappingService(adapters: List<ScheduleAdapter>): ScheduleMappingService = ScheduleMappingService(adapters)
    }
}
