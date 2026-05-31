package yayauheny.by.bot.handler

import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.message.Message
import yayauheny.by.bot.callback.CallbackData

data class UpdateContext(
    val chatId: Long,
    val userId: Long,
    val username: String?,
    val text: String?,
    val hasLocation: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val isCallback: Boolean,
    val callbackData: String?,
    val messageId: Int?
) {
    companion object {
        fun from(update: Update): UpdateContext {
            return when {
                update.hasMessage() -> fromMessage(update.message)
                update.hasCallbackQuery() -> fromCallbackQuery(update.callbackQuery)
                else -> UpdateContext(0, 0, null, null, false, null, null, false, null, null)
            }
        }

        private fun fromMessage(message: Message): UpdateContext {
            val chatId = message.chatId
            val userId = extractUserId(message.from)
            val username = extractUsername(message.from)
            val text = message.text
            val messageId = message.messageId

            val locationData = extractLocationData(message)

            return UpdateContext(
                chatId = chatId,
                userId = userId,
                username = username,
                text = text,
                hasLocation = locationData.hasLocation,
                latitude = locationData.latitude,
                longitude = locationData.longitude,
                isCallback = false,
                callbackData = null,
                messageId = messageId
            )
        }

        private fun fromCallbackQuery(callbackQuery: org.telegram.telegrambots.meta.api.objects.CallbackQuery): UpdateContext {
            val chatId = callbackQuery.message.chatId
            val userId = extractUserId(callbackQuery.from)
            val username = extractUsername(callbackQuery.from)
            val callbackData = callbackQuery.data
            val messageId = callbackQuery.message.messageId

            return UpdateContext(
                chatId = chatId,
                userId = userId,
                username = username,
                text = null,
                hasLocation = false,
                latitude = null,
                longitude = null,
                isCallback = true,
                callbackData = callbackData,
                messageId = messageId
            )
        }

        private fun extractUserId(user: org.telegram.telegrambots.meta.api.objects.User?): Long {
            requireNotNull(user) { "User is null - this should not happen in normal bot scenarios" }
            return user.id
        }

        private fun extractUsername(user: org.telegram.telegrambots.meta.api.objects.User?): String? {
            return user?.userName
        }

        private fun extractLocationData(message: Message): LocationData {
            if (message.hasLocation()) {
                val location = message.location
                return LocationData(true, location.latitude, location.longitude)
            } else if (message.venue?.location != null) {
                val venueLocation = message.venue.location
                return LocationData(true, venueLocation.latitude, venueLocation.longitude)
            } else {
                return LocationData(false, null, null)
            }
        }
    }

    private data class LocationData(val hasLocation: Boolean, val latitude: Double?, val longitude: Double?)

    fun isTextCommand(command: String): Boolean {
        return text != null && text == command
    }

    fun isCallbackOfType(type: String): Boolean {
        return isCallback && callbackData != null && CallbackData.isType(callbackData, type)
    }

    fun callbackArg(): String {
        return if (callbackData != null) CallbackData.arg(callbackData) else ""
    }
}
