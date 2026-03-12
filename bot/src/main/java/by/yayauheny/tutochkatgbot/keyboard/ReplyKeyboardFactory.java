package by.yayauheny.tutochkatgbot.keyboard;

import by.yayauheny.tutochkatgbot.messages.Messages;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * Factory for reply keyboards
 */
@Component
public class ReplyKeyboardFactory {

    /**
     * Create keyboard for sharing location
     * @return reply keyboard markup
     */
    public ReplyKeyboardMarkup shareLocation() {
        KeyboardButton button = KeyboardButton.builder()
                .text(Messages.BUTTON_SHARE_LOCATION)
                .requestLocation(true)
                .build();
        
        KeyboardRow row = new KeyboardRow();
        row.add(button);
        
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    /**
     * Create keyboard for help and location
     * @return reply keyboard markup
     */
    public ReplyKeyboardMarkup helpAndLocation() {
        KeyboardButton locationButton = KeyboardButton.builder()
                .text(Messages.BUTTON_SHARE_LOCATION)
                .requestLocation(true)
                .build();
        
        KeyboardButton helpButton = KeyboardButton.builder()
                .text("/help")
                .build();
        
        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);
        row.add(helpButton);
        
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }
}
