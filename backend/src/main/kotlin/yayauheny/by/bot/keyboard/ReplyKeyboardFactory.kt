package yayauheny.by.bot.keyboard

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService

@Component
class ReplyKeyboardFactory(
    private val botAdminAccessService: BotAdminAccessService
) {

    fun shareLocation(
        userId: Long,
        isPrivateChat: Boolean
    ): ReplyKeyboardMarkup {
        val button = KeyboardButton.builder()
            .text(Messages.BUTTON_SHARE_LOCATION)
            .requestLocation(true)
            .build()

        val row = KeyboardRow()
        row.add(button)

        if (isPrivateChat && botAdminAccessService.isAdmin(userId)) {
            row.add(
                KeyboardButton.builder()
                    .text(Messages.BUTTON_ANALYTICS)
                    .build()
            )
        }

        return buildKeyboard(listOf(row))
    }

    fun helpAndLocation(
        userId: Long,
        isPrivateChat: Boolean
    ): ReplyKeyboardMarkup {
        val locationButton = KeyboardButton.builder()
            .text(Messages.BUTTON_SHARE_LOCATION)
            .requestLocation(true)
            .build()

        val helpButton = KeyboardButton.builder()
            .text("/help")
            .build()

        val row = KeyboardRow()
        row.add(locationButton)
        row.add(helpButton)

        return buildKeyboard(listOf(row))
    }

    private fun buildKeyboard(baseRows: List<KeyboardRow>): ReplyKeyboardMarkup {
        return ReplyKeyboardMarkup.builder()
            .keyboard(baseRows)
            .resizeKeyboard(true)
            .oneTimeKeyboard(false)
            .build()
    }
}
