package integration.telegram

import com.fasterxml.jackson.databind.ObjectMapper
import integration.base.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.location.Location
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.analytics.repository.AnalyticsEventJpaRepository
import yayauheny.by.analytics.repository.UserAnalyticsJpaRepository
import yayauheny.by.analytics.repository.UserJpaRepository
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.messages.Messages
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.RestroomStatus
import java.util.UUID

@Tag("integration")
@Import(TelegramWebhookFlowIntegrationTest.TestConfig::class)
@TestPropertySource(
    properties = [
        "telegram.bot.enabled=true",
        "telegram.bot.token=test-token",
        "telegram.bot.webhook-secret-token=secret-token",
        "telegram.bot.webhook-path=/telegram/webhook",
        "telegram.bot.webhook-public-url="
    ]
)
class TelegramWebhookFlowIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var messageSender: RecordingMessageSender

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @Autowired
    private lateinit var encryptionService: yayauheny.by.util.EncryptionService

    private lateinit var environment: DatabaseTestHelper.TestEnvironment

    private var nearestRestroomId: UUID? = null
    private var secondNearestRestroomId: UUID? = null

    @BeforeEach
    fun setUp() {
        messageSender.clear()
        DatabaseTestHelper.truncateAllTables()
        environment = DatabaseTestHelper.createTestEnvironment()
    }

    private fun seedStableSearchArea() {
        val cityId = environment.cityId

        val buildingId = DatabaseTestHelper.insertTestBuilding(
            cityId = cityId,
            address = "Lenina 1",
            name = "Test Mall",
            lat = 53.900,
            lon = 27.560
        )

        nearestRestroomId = DatabaseTestHelper.insertTestRestroom(
            cityId = cityId,
            data = DatabaseTestHelper.createTestRestroomData(
                name = "Nearest Restroom",
                address = "Lenina 1",
                feeType = FeeType.FREE,
                status = RestroomStatus.ACTIVE,
                buildingId = buildingId,
                lat = 53.9001,
                lon = 27.5601,
                originProvider = ImportProvider.TWO_GIS
            )
        )
        DatabaseTestHelper.jdbcTemplate.update(
            """
            UPDATE restrooms
            SET work_time = ?::jsonb
            WHERE id = ?
            """.trimIndent(),
            """
            {
              "is_24x7": true
            }
            """.trimIndent(),
            nearestRestroomId
        )

        secondNearestRestroomId = DatabaseTestHelper.insertTestRestroom(
            cityId = cityId,
            data = DatabaseTestHelper.createTestRestroomData(
                name = "Second Nearest",
                address = "Lenina 2",
                feeType = FeeType.PAID,
                status = RestroomStatus.ACTIVE,
                lat = 53.902,
                lon = 27.562
            )
        )

        DatabaseTestHelper.insertTestRestroom(
            cityId = cityId,
            data = DatabaseTestHelper.createTestRestroomData(
                name = "Farther",
                lat = 53.9135,
                lon = 27.560,
                status = RestroomStatus.ACTIVE
            )
        )

        val hiddenId = DatabaseTestHelper.insertTestRestroom(
            cityId = cityId,
            data = DatabaseTestHelper.createTestRestroomData(
                name = "Hidden",
                lat = 53.901,
                lon = 27.561,
                status = RestroomStatus.ACTIVE
            )
        )
        DatabaseTestHelper.jdbcTemplate.update("UPDATE restrooms SET is_hidden = true WHERE id = ?", hiddenId)

        DatabaseTestHelper.insertTestRestroom(
            cityId = cityId,
            data = DatabaseTestHelper.createTestRestroomData(
                name = "Inactive",
                lat = 53.901,
                lon = 27.561,
                status = RestroomStatus.TEMP_CLOSED
            )
        )
    }

    @Test
    fun `start command goes through webhook and sends welcome message`() {
        val update = commandUpdate("/start website", userId = 10L, chatId = 123L, username = "test-user-10")
        performWebhook(update)

        val call = messageSender.sendTextCalls.single()
        assertThat(call.chatId).isEqualTo(123L)
        assertThat(call.text).isEqualTo(Messages.WELCOME_MESSAGE)

        val user = userRepository.findByTgUserId(encryptionService.encrypt("10"))!!
        assertThat(user.acquisitionTag).isEqualTo("website")
        assertThat(user.source).isEqualTo("telegram_bot")
        assertThat(analyticsEventRepository.findAll().map { it.event })
            .containsExactly(ProductAnalyticsEvent.BOT_STARTED.value)
    }

    @Test
    fun `start command accepts real telegram payload fixture`() {
        performWebhook(loadWebhookFixture("telegram/start_website_update.json"))

        val call = messageSender.sendTextCalls.single()
        assertThat(call.chatId).isEqualTo(123L)
        assertThat(call.text).isEqualTo(Messages.WELCOME_MESSAGE)

        val user = userRepository.findByTgUserId(encryptionService.encrypt("10"))!!
        assertThat(user.username).isEqualTo("test-user-10")
        assertThat(analyticsEventRepository.findAll().map { it.event })
            .containsExactly(ProductAnalyticsEvent.BOT_STARTED.value)
    }

    @Test
    fun `help command goes through webhook and sends help message`() {
        val update = commandUpdate("/help", userId = 20L, chatId = 124L, username = "test-user-20")
        performWebhook(update)

        val call = messageSender.sendTextCalls.single()
        assertThat(call.chatId).isEqualTo(124L)
        assertThat(call.text).isEqualTo(Messages.HELP_MESSAGE)
    }

    @Test
    fun `location search success real webhook flow`() {
        seedStableSearchArea()

        val update = locationUpdate(userId = 100L, chatId = 123L, username = "test-100", lat = 53.900, lon = 27.560)
        performWebhook(update)

        val call = messageSender.sendTextCalls.single()
        assertThat(call.chatId).isEqualTo(123L)
        assertThat(call.text).contains("Найдено 3 туалетов поблизости:")
        assertThat(call.text).doesNotContain("Hidden")
        assertThat(call.text).doesNotContain("Inactive")
        
        val buttons = call.inlineKeyboard?.keyboard ?: emptyList()
        val detailCallbacks = buttons.flatten()
            .map { it.callbackData }
            .filter { it.startsWith("detail:") }

        assertThat(detailCallbacks).hasSize(3)
        assertThat(detailCallbacks).startsWith(
            "detail:$nearestRestroomId",
            "detail:$secondNearestRestroomId"
        )

        val users = userRepository.findAll()
        assertThat(users).hasSize(1)
        val user = users[0]
        assertThat(user.tgUserId).isEqualTo(encryptionService.encrypt("100"))

        val userAnalytics = userAnalyticsRepository.findById(user.id!!).get()
        assertThat(userAnalytics.searchesCount).isEqualTo(1)
        assertThat(userAnalytics.successfulSearchesCount).isEqualTo(1)
        assertThat(userAnalytics.emptySearchesCount).isEqualTo(0)
        assertThat(analyticsEventRepository.findAll().map { it.event })
            .containsExactly(ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value)
    }

    @Test
    fun `location search accepts real telegram payload fixture`() {
        seedStableSearchArea()

        performWebhook(loadWebhookFixture("telegram/location_update.json"))

        val call = messageSender.sendTextCalls.single()
        assertThat(call.chatId).isEqualTo(123L)
        assertThat(call.text).contains("Найдено 3 туалетов поблизости:")
    }

    @Test
    fun `location search empty result flow`() {
        val cityId = environment.cityId
        DatabaseTestHelper.insertTestRestroom(
            cityId = cityId,
            data = DatabaseTestHelper.createTestRestroomData(
                name = "Farther",
                lat = 54.3,
                lon = 28.3,
                status = RestroomStatus.ACTIVE
            )
        )

        val update = locationUpdate(userId = 200L, chatId = 124L, username = "test-200", lat = 53.900, lon = 27.560)
        performWebhook(update)

        val calls = messageSender.sendTextCalls
        assertThat(calls).hasSize(1)
        assertThat(calls[0].text).isEqualTo(Messages.NO_TOILETS_FOUND)

        val user = userRepository.findByTgUserId(encryptionService.encrypt("200"))!!
        val analytics = userAnalyticsRepository.findById(user.id!!).get()
        assertThat(analytics.searchesCount).isEqualTo(1)
        assertThat(analytics.successfulSearchesCount).isEqualTo(0)
        assertThat(analytics.emptySearchesCount).isEqualTo(1)
    }

    @Test
    fun `details-after-search flow`() {
        seedStableSearchArea()

        val locUpdate = locationUpdate(userId = 300L, chatId = 125L, username = "test-300", lat = 53.900, lon = 27.560)
        performWebhook(locUpdate)

        val call = messageSender.sendTextCalls.single()
        val detailCallbackData = call.inlineKeyboard?.keyboard?.first()?.first()?.callbackData!!
        
        messageSender.clear()

        val callbackUpdate = callbackUpdate(detailCallbackData, userId = 300L, chatId = 125L, username = "test-300")
        performWebhook(callbackUpdate)

        val answerCall = messageSender.answerCallbackQueries.single()
        assertThat(answerCall.callbackQueryId).isEqualTo("query-id")

        val editCall = messageSender.editOrReplyCalls.single()
        assertThat(editCall.text).contains("Test Mall")
        assertThat(editCall.text).contains("Lenina 1")
        assertThat(editCall.text).contains("🟢 Туалет открыт сейчас")
        assertThat(editCall.text).contains("<b>Время работы:</b>")
        assertThat(editCall.text).contains("Круглосуточно")

        val user = userRepository.findByTgUserId(encryptionService.encrypt("300"))!!
        val analytics = userAnalyticsRepository.findById(user.id!!).get()
        assertThat(analytics.detailsOpenedCount).isEqualTo(1)
        assertThat(analyticsEventRepository.findAll().map { it.event })
            .containsExactly(
                ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value,
                ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value
            )
    }

    @Test
    fun `back-to-list flow restores cache`() {
        seedStableSearchArea()

        val locUpdate = locationUpdate(userId = 400L, chatId = 126L, username = "test-400", lat = 53.900, lon = 27.560)
        performWebhook(locUpdate)
        
        val listCall = messageSender.sendTextCalls.single()
        val listCallText = listCall.text
        val detailCallbackData = listCall.inlineKeyboard?.keyboard?.first()?.first()?.callbackData!!
        messageSender.clear()

        val detailUpdate = callbackUpdate(detailCallbackData, userId = 400L, chatId = 126L, username = "test-400")
        performWebhook(detailUpdate)
        messageSender.clear()

        val backUpdate = callbackUpdate("back:list", userId = 400L, chatId = 126L, username = "test-400")
        performWebhook(backUpdate)

        val backCall = messageSender.editOrReplyCalls.single()
        assertThat(backCall.text).isEqualTo(listCallText)
        assertThat(backCall.inlineKeyboard?.keyboard?.first()?.first()?.callbackData).isEqualTo(detailCallbackData)
    }

    @Test
    fun `malformed callback payload does not crash`() {
        val update = callbackUpdate("detail:invalid-uuid", userId = 600L, chatId = 128L, username = "test-600")
        performWebhook(update)

        val editCall = messageSender.editOrReplyCalls.single()
        assertThat(editCall.text).isEqualTo(Messages.SOMETHING_WENT_WRONG)
    }

    @Test
    fun `unsupported update does not create analytics`() {
        val update = Update()
        performWebhook(update)

        assertThat(messageSender.safeReplyCalls).isEmpty()
        assertThat(messageSender.sendTextCalls).isEmpty()
        assertThat(messageSender.editOrReplyCalls).isEmpty()
        val count = userRepository.count()
        assertThat(count).isEqualTo(0)
    }

    private fun performWebhook(update: Update) {
        mockMvc.perform(
            post("/telegram/webhook")
                .header("X-Telegram-Bot-Api-Secret-Token", "secret-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
        )
            .andExpect(status().isOk)
    }

    private fun performWebhook(rawPayload: String) {
        mockMvc.perform(
            post("/telegram/webhook")
                .header("X-Telegram-Bot-Api-Secret-Token", "secret-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload)
        )
            .andExpect(status().isOk)
    }

    private fun loadWebhookFixture(resourcePath: String): String =
        object {}.javaClass.classLoader
            .getResourceAsStream(resourcePath)
            ?.reader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: error("Resource not found: $resourcePath")

    private fun commandUpdate(text: String, userId: Long, chatId: Long, username: String?): Update {
        val update = Update()
        val message = Message()
        message.text = text
        message.chat = Chat.builder().id(chatId).type("private").build()
        message.messageId = 1
        message.from = User.builder().id(userId).isBot(false).firstName("Test").userName(username).build()
        message.entities =
            listOf(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length(text.split(" ").first().length)
                    .build()
            )
        update.message = message
        return update
    }

    private fun locationUpdate(userId: Long, chatId: Long, username: String?, lat: Double, lon: Double): Update {
        val update = Update()
        val message = Message()
        message.chat = Chat.builder().id(chatId).type("private").build()
        message.messageId = 1
        message.from = User.builder().id(userId).isBot(false).firstName("Test").userName(username).build()
        message.location = Location.builder().latitude(lat).longitude(lon).build()
        update.message = message
        return update
    }

    private fun callbackUpdate(callbackData: String, userId: Long, chatId: Long, username: String?): Update {
        val update = Update()
        val callbackQuery = CallbackQuery()
        callbackQuery.data = callbackData
        callbackQuery.id = "query-id"
        val message = Message()
        message.messageId = 1
        message.chat = Chat.builder().id(chatId).type("private").build()
        callbackQuery.message = message
        callbackQuery.from = User.builder().id(userId).isBot(false).firstName("Test").userName(username).build()
        update.callbackQuery = callbackQuery
        return update
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun messageSender(): RecordingMessageSender = RecordingMessageSender()
    }
}

class RecordingMessageSender : MessageSender {
    data class SendTextCall(
        val chatId: Long,
        val text: String,
        val replyKeyboard: ReplyKeyboardMarkup? = null,
        val inlineKeyboard: InlineKeyboardMarkup? = null
    )

    data class EditOrReplyCall(
        val context: UpdateContext,
        val text: String,
        val inlineKeyboard: InlineKeyboardMarkup?
    )

    data class SafeReplyCall(
        val context: UpdateContext,
        val errorMessage: String
    )

    data class AnswerCallbackCall(
        val callbackQueryId: String,
        val text: String?
    )

    val sendTextCalls = mutableListOf<SendTextCall>()
    val editOrReplyCalls = mutableListOf<EditOrReplyCall>()
    val safeReplyCalls = mutableListOf<SafeReplyCall>()
    val answerCallbackQueries = mutableListOf<AnswerCallbackCall>()

    fun clear() {
        sendTextCalls.clear()
        editOrReplyCalls.clear()
        safeReplyCalls.clear()
        answerCallbackQueries.clear()
    }

    // Intentionally fail if the webhook flow starts using the low-level Telegram API methods directly.
    // These tests record the higher-level MessageSender operations used by bot handlers.
    override fun sendMessage(message: SendMessage) {
        throw UnsupportedOperationException("not used in tests")
    }

    override fun editMessage(message: EditMessageText) {
        throw UnsupportedOperationException("not used in tests")
    }

    override fun sendText(chatId: Long, text: String) {
        sendTextCalls += SendTextCall(chatId, text)
    }

    override fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup) {
        sendTextCalls += SendTextCall(chatId, text, replyKeyboard = replyKeyboard)
    }

    override fun sendText(chatId: Long, text: String, inlineKeyboard: InlineKeyboardMarkup) {
        sendTextCalls += SendTextCall(chatId, text, inlineKeyboard = inlineKeyboard)
    }

    override fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup, inlineKeyboard: InlineKeyboardMarkup) {
        sendTextCalls += SendTextCall(chatId, text, replyKeyboard = replyKeyboard, inlineKeyboard = inlineKeyboard)
    }

    override fun editOrReply(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?) {
        editOrReplyCalls += EditOrReplyCall(ctx, text, inlineKeyboard)
    }

    override fun editMessage(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?) {
        editOrReplyCalls += EditOrReplyCall(ctx, text, inlineKeyboard)
    }

    override fun safeReply(ctx: UpdateContext, errorMessage: String) {
        safeReplyCalls += SafeReplyCall(ctx, errorMessage)
    }

    override fun answerCallbackQuery(callbackQueryId: String, text: String?) {
        answerCallbackQueries += AnswerCallbackCall(callbackQueryId, text)
    }
}
