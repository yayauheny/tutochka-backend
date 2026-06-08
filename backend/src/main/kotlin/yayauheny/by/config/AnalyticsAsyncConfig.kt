package yayauheny.by.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
class AnalyticsAsyncConfig {
    @Bean(name = ["analyticsTaskExecutor"])
    fun analyticsTaskExecutor(
        @Value("\${analytics.async.enabled:true}") analyticsAsyncEnabled: Boolean
    ): TaskExecutor {
        if (!analyticsAsyncEnabled) {
            return SyncTaskExecutor()
        }

        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 1
            maxPoolSize = 2
            queueCapacity = 500
            setThreadNamePrefix("analytics-")
            initialize()
        }
    }
}
