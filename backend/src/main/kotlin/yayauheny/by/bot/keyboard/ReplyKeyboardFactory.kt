package yayauheny.by.bot.keyboard

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import yayauheny.by.bot.messages.Messages

@Component
class ReplyKeyboardFactory {

    fun shareLocation(): ReplyKeyboardMarkup {
        val button = KeyboardButton.builder()
            .text(Messages.BUTTON_SHARE_LOCATION)
            .requestLocation(true)
            .build()
        
        val row = KeyboardRow()
        row.add(button)
        
        return ReplyKeyboardMarkup.builder()
            .keyboard(listOf(row))
            .resizeKeyboard(true)
            .oneTimeKeyboard(false)
            .build()
    }

    fun helpAndLocation(): ReplyKeyboardMarkup {
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
        
        return ReplyKeyboardMarkup.builder()
            .keyboard(listOf(row))
            .resizeKeyboard(true)
            .oneTimeKeyboard(false)
            .build()
    }
}
