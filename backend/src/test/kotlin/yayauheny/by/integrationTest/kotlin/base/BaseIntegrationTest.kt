package integration.base

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import yayauheny.by.Application
import yayauheny.by.analytics.repository.AnalyticsEventJpaRepository
import yayauheny.by.analytics.repository.UserAnalyticsJpaRepository
import yayauheny.by.analytics.repository.UserJpaRepository
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.SubwayLineJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository

object IntegrationTestContext {
    lateinit var mockMvc: MockMvc
}

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    protected lateinit var countryRepository: CountryJpaRepository

    @Autowired
    protected lateinit var cityRepository: CityJpaRepository

    @Autowired
    protected lateinit var restroomRepository: RestroomJpaRepository

    @Autowired
    protected lateinit var buildingRepository: BuildingJpaRepository

    @Autowired
    protected lateinit var subwayLineRepository: SubwayLineJpaRepository

    @Autowired
    protected lateinit var subwayStationRepository: SubwayStationJpaRepository

    @Autowired
    protected lateinit var userRepository: UserJpaRepository

    @Autowired
    protected lateinit var userAnalyticsRepository: UserAnalyticsJpaRepository

    @Autowired
    protected lateinit var analyticsEventRepository: AnalyticsEventJpaRepository

    protected val postgres
        get() = TestPostgresContainer.postgres

    @BeforeEach
    fun wireDatabaseHelper() {
        IntegrationTestContext.mockMvc = mockMvc
        DatabaseTestHelper.jdbcTemplate = jdbcTemplate
        DatabaseTestHelper.countryRepository = countryRepository
        DatabaseTestHelper.cityRepository = cityRepository
        DatabaseTestHelper.restroomRepository = restroomRepository
        DatabaseTestHelper.buildingRepository = buildingRepository
        DatabaseTestHelper.subwayLineRepository = subwayLineRepository
        DatabaseTestHelper.subwayStationRepository = subwayStationRepository
        DatabaseTestHelper.userRepository = userRepository
        DatabaseTestHelper.userAnalyticsRepository = userAnalyticsRepository
        DatabaseTestHelper.analyticsEventRepository = analyticsEventRepository
    }

    @AfterEach
    fun cleanDatabase() {
        DatabaseTestHelper.truncateAllTables()
    }

    protected fun withRoutingApp(block: MockMvc.() -> Unit) {
        mockMvc.block()
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            val container = TestPostgresContainer.postgres
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.driver-class-name", container::getDriverClassName)
        }
    }
}
