package yayauheny.by.unit.controller

import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import javax.sql.DataSource
import yayauheny.by.analytics.api.AnalyticsController
import yayauheny.by.analytics.api.PublicAnalyticsController
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.service.PublicAnalyticsService
import yayauheny.by.common.config.ApiProperties
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.HealthController
import yayauheny.by.controller.RestroomController
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService

abstract class RoutingTestBase {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockBean
    protected lateinit var apiProperties: ApiProperties

    @MockBean
    protected lateinit var countryService: CountryService

    @MockBean
    protected lateinit var cityService: CityService

    @MockBean
    protected lateinit var restroomService: RestroomService

    @MockBean
    protected lateinit var publicAnalyticsService: PublicAnalyticsService

    @MockBean
    protected lateinit var analyticsService: AnalyticsService

    @MockBean
    protected lateinit var businessAnalyticsPublisher: BusinessAnalyticsPublisher

    @MockBean
    protected lateinit var dataSource: DataSource

    @BeforeEach
    fun stubApiProperties() {
        given(apiProperties.defaults).willReturn(
            ApiProperties.Defaults(
                page = 0,
                pageSize = 20,
                maxNearestRestroomsSize = 50,
                maxDistanceMeters = 5000
            )
        )
        given(apiProperties.limits).willReturn(
            ApiProperties.Limits(
                maxNameLength = 100,
                minNameLength = 0,
                minNameLengthRequired = 1,
                maxRegionLength = 100,
                maxCountryCodeLength = 2,
                maxDescriptionLength = 1000,
                maxAddressLength = 255,
                maxJsonStringLength = 5000,
                maxPageSize = 100
            )
        )
    }

    protected fun withRoutingApp(block: MockMvc.() -> Unit) {
        mockMvc.block()
    }
}
