package yayauheny.by.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.common.config.TelegramBotProperties
import yayauheny.by.common.config.TelegramWebhookSecretTokenFilter
import yayauheny.by.service.BotUpdateService
import yayauheny.by.metrics.BackendSearchMetrics
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@WebMvcTest(TelegramWebhookController::class)
@EnableConfigurationProperties(TelegramBotProperties::class)
@Import(TelegramWebhookSecretTokenFilter::class, TelegramWebhookControllerTest.TestConfig::class)
@TestPropertySource(properties = [
    "telegram.bot.token=test-token",
    "telegram.bot.webhook-secret-token=secret-token",
    "telegram.bot.webhook-path=/telegram/webhook"
])
class TelegramWebhookControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun botUpdateService(): BotUpdateService = mockk()

        @Bean
        fun backendSearchMetrics(): BackendSearchMetrics = mockk(relaxed = true)
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var botUpdateService: BotUpdateService

    @BeforeEach
    fun setup() {
        clearMocks(botUpdateService)
        every { botUpdateService.process(any()) } just runs
    }

    @Test
    fun `accepts valid webhook request`() {
        val update = Update().apply { updateId = 123 }
        
        mockMvc.perform(
            post("/telegram/webhook")
                .header("X-Telegram-Bot-Api-Secret-Token", "secret-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
        )
            .andExpect(status().isOk)

        verify { botUpdateService.process(any()) }
    }

    @Test
    fun `rejects missing secret`() {
        val update = Update().apply { updateId = 123 }
        
        mockMvc.perform(
            post("/telegram/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { botUpdateService.process(any()) }
    }

    @Test
    fun `rejects invalid secret`() {
        val update = Update().apply { updateId = 123 }
        
        mockMvc.perform(
            post("/telegram/webhook")
                .header("X-Telegram-Bot-Api-Secret-Token", "wrong-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { botUpdateService.process(any()) }
    }

    @Test
    fun `rejects malformed update body`() {
        mockMvc.perform(
            post("/telegram/webhook")
                .header("X-Telegram-Bot-Api-Secret-Token", "secret-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }")
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { botUpdateService.process(any()) }
    }
}
