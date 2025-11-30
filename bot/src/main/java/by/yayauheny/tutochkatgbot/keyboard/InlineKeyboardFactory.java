package by.yayauheny.tutochkatgbot.keyboard;

import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.integration.dto.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.integration.dto.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.util.DistanceFormat;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

/**
 * Factory for inline keyboards
 */
@Component
public class InlineKeyboardFactory {
    private final FormatterService formatterService;

    public InlineKeyboardFactory(FormatterService formatterService) {
        this.formatterService = formatterService;
    }

    /**
     * Create keyboard for toilet list
     * @param toilets list of toilets
     * @return inline keyboard markup
     */
    public InlineKeyboardMarkup toiletList(List<NearestRestroomResponseDto> toilets) {
        List<List<InlineKeyboardButton>> rows = toilets.stream()
            .map(this::createToiletButton)
            .map(List::of)
            .toList();
        
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    /**
     * Create keyboard for toilet details
     * @param toilet toilet data
     * @return inline keyboard markup
     */
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
                    List.of(mapsButton),
                    List.of(backButton)
                ))
                .build();
    }

    /**
     * Create keyboard for radius selection
     * @return inline keyboard markup
     */
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
                    List.of(radius500, radius1km),
                    List.of(radius2km, radius5km)
                ))
                .build();
    }

    private InlineKeyboardButton createToiletButton(NearestRestroomResponseDto toilet) {
        String buttonText = formatterService.toiletListItem(toilet);
        
        return InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(CallbackData.detail(toilet.id()))
                .build();
    }
}
