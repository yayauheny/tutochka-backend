package by.yayauheny.tutochkatgbot.keyboard;

import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.util.Links;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;
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

    public InlineKeyboardMarkup toiletList(List<NearestRestroomSlimDto> toilets) {
        List<InlineKeyboardRow> rows = toilets.stream()
            .map(this::createToiletButton)
            .map(button -> new InlineKeyboardRow(List.of(button)))
            .toList();
        
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup toiletDetail(RestroomResponseDto toilet) {
        double lat = toilet.coordinates().lat();
        double lon = toilet.coordinates().lon();

        InlineKeyboardButton yandexButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_OPEN_YANDEX)
                .url(Links.yandexMaps(lat, lon))
                .build();

        InlineKeyboardButton googleButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_OPEN_GOOGLE)
                .url(Links.googleMaps(lat, lon))
                .build();

        String twoGisUrl = extract2GisBranchId(toilet.externalMaps())
                .map(Links::twoGisById)
                .orElseGet(() -> Links.twoGis(lat, lon));

        InlineKeyboardButton twoGisButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_OPEN_2GIS)
                .url(twoGisUrl)
                .build();

        InlineKeyboardButton appleButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_OPEN_APPLE)
                .url(Links.appleMaps(lat, lon))
                .build();
        
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(Messages.BUTTON_BACK)
                .callbackData(CallbackData.backToList())
                .build();
        
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                    new InlineKeyboardRow(List.of(yandexButton, googleButton)),
                    new InlineKeyboardRow(List.of(twoGisButton, appleButton)),
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

    private InlineKeyboardButton createToiletButton(NearestRestroomSlimDto toilet) {
        String buttonText = formatterService.toiletListItem(toilet);
        
        return InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(CallbackData.detail(toilet.id().toString()))
                .build();
    }

    private Optional<String> extract2GisBranchId(Map<String, Object> externalMaps) {
        if (externalMaps == null) {
            return Optional.empty();
        }

        Object twoGis = externalMaps.get("2gis");
        if (!(twoGis instanceof Map<?, ?> twoGisMap)) {
            return Optional.empty();
        }

        Object branchId = twoGisMap.get("branch_id");
        return (branchId instanceof String s && !s.isBlank())
                ? Optional.of(s)
                : Optional.empty();
    }
}
