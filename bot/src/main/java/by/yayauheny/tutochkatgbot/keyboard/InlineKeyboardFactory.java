package by.yayauheny.tutochkatgbot.keyboard;

import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * Factory for inline keyboards
 */
@Component
public class InlineKeyboardFactory {
    private final FormatterService formatterService;

    public InlineKeyboardFactory(FormatterService formatterService) {
        this.formatterService = formatterService;
    }

    public InlineKeyboardMarkup toiletList(List<NearestRestroomResponseDto> toilets) {
        List<InlineKeyboardRow> rows = toilets.stream()
            .map(this::createToiletButton)
            .map(button -> new InlineKeyboardRow(List.of(button)))
            .toList();
        
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup toiletDetails(RestroomResponseDto toilet) {
        InlineKeyboardButton mapsButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_OPEN_MAPS)
                .url(formatterService.generateMapsLink(toilet))
                .build();
        
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_BACK_TO_LIST)
                .callbackData(CallbackData.backToList())
                .build();
        
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                    new InlineKeyboardRow(List.of(mapsButton)),
                    new InlineKeyboardRow(List.of(backButton))
                ))
                .build();
    }

    public InlineKeyboardMarkup radiusSelection() {
        InlineKeyboardButton radius500 = InlineKeyboardButton.builder()
                .text(Messages.RADIUS_500M)
                .callbackData(CallbackData.radius(500))
                .build();
        
        InlineKeyboardButton radius1km = InlineKeyboardButton.builder()
                .text(Messages.RADIUS_1KM)
                .callbackData(CallbackData.radius(1000))
                .build();
        
        InlineKeyboardButton radius2km = InlineKeyboardButton.builder()
                .text(Messages.RADIUS_2KM)
                .callbackData(CallbackData.radius(2000))
                .build();
        
        InlineKeyboardButton radius5km = InlineKeyboardButton.builder()
                .text(Messages.RADIUS_5KM)
                .callbackData(CallbackData.radius(5000))
                .build();
        
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                    new InlineKeyboardRow(List.of(radius500, radius1km)),
                    new InlineKeyboardRow(List.of(radius2km, radius5km))
                ))
                .build();
    }

    private InlineKeyboardButton createToiletButton(NearestRestroomResponseDto toilet) {
        String buttonText = formatterService.toiletListItem(toilet);
        
        return InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(CallbackData.detail(toilet.id().toString()))
                .build();
    }
}
