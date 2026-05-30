package yayauheny.by.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.api")
data class ApiProperties(
    val defaults: Defaults = Defaults(),
    val limits: Limits = Limits()
) {
    data class Defaults(
        val page: Int = 0,
        val pageSize: Int = 20,
        val maxNearestRestroomsSize: Int = 50,
        val maxDistanceMeters: Int = 5000
    )

    data class Limits(
        val maxNameLength: Int = 100,
        val minNameLength: Int = 0,
        val minNameLengthRequired: Int = 1,
        val maxRegionLength: Int = 100,
        val maxCountryCodeLength: Int = 2,
        val maxDescriptionLength: Int = 1000,
        val maxAddressLength: Int = 255,
        val maxJsonStringLength: Int = 5000
    )
}
