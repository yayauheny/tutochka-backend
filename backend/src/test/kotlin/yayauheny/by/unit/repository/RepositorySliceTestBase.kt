package yayauheny.by.unit.repository

import integration.base.TestPostgresContainer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import yayauheny.by.analytics.repository.AnalyticsEventJpaRepository
import yayauheny.by.analytics.repository.PublicAnalyticsRepositoryImpl
import yayauheny.by.analytics.repository.UserJpaRepository
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.SubwayLineJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PublicAnalyticsRepositoryImpl::class)
abstract class RepositorySliceTestBase {
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
    protected lateinit var analyticsEventRepository: AnalyticsEventJpaRepository

    @BeforeEach
    fun wireDatabaseHelper() {
        DatabaseTestHelper.jdbcTemplate = jdbcTemplate
        DatabaseTestHelper.countryRepository = countryRepository
        DatabaseTestHelper.cityRepository = cityRepository
        DatabaseTestHelper.restroomRepository = restroomRepository
        DatabaseTestHelper.buildingRepository = buildingRepository
        DatabaseTestHelper.subwayLineRepository = subwayLineRepository
        DatabaseTestHelper.subwayStationRepository = subwayStationRepository
        DatabaseTestHelper.userRepository = userRepository
        DatabaseTestHelper.analyticsEventRepository = analyticsEventRepository
    }

    @AfterEach
    fun cleanDatabase() {
        DatabaseTestHelper.truncateAllTables()
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
