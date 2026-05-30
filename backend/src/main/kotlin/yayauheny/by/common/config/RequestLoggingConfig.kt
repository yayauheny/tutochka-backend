package yayauheny.by.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class RequestLoggingConfig {
    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter =
        CommonsRequestLoggingFilter().apply {
            setIncludeClientInfo(true)
            setIncludeQueryString(true)
            setIncludeHeaders(false)
            setIncludePayload(false)
            setAfterMessagePrefix("HTTP request: ")
        }
}
