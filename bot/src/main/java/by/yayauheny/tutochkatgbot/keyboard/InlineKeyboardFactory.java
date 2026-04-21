package by.yayauheny.tutochkatgbot.keyboard;

import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.util.Links;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

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
        List<InlineKeyboardRow> rows = new ArrayList<>();

        addRow(rows,
            createMapButton(Messages.BUTTON_OPEN_YANDEX, yandexUrl(toilet)),
            createMapButton(Messages.BUTTON_OPEN_GOOGLE, googleUrl(toilet))
        );
        addRow(rows,
            createMapButton(Messages.BUTTON_OPEN_2GIS, twoGisUrl(toilet)),
            createMapButton(Messages.BUTTON_OPEN_APPLE, appleUrl(toilet))
        );
        rows.add(new InlineKeyboardRow(List.of(
            InlineKeyboardButton.builder()
                .text(Messages.BUTTON_BACK)
                .callbackData(CallbackData.backToList())
                .build()
        )));

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
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

    private void addRow(List<InlineKeyboardRow> rows, InlineKeyboardButton... buttons) {
        List<InlineKeyboardButton> rowButtons = new ArrayList<>();
        for (InlineKeyboardButton button : buttons) {
            if (button != null) {
                rowButtons.add(button);
            }
        }
        if (!rowButtons.isEmpty()) {
            rows.add(new InlineKeyboardRow(rowButtons));
        }
    }

    private InlineKeyboardButton createMapButton(String text, Optional<String> url) {
        return url.filter(value -> !value.isBlank())
            .map(value -> InlineKeyboardButton.builder()
                .text(text)
                .url(value)
                .build())
            .orElse(null);
    }

    protected Optional<String> yandexUrl(RestroomResponseDto toilet) {
        return mapUrl(toilet, Links::yandexMaps);
    }

    protected Optional<String> googleUrl(RestroomResponseDto toilet) {
        return mapUrl(toilet, Links::googleMaps);
    }

    protected Optional<String> appleUrl(RestroomResponseDto toilet) {
        return mapUrl(toilet, Links::appleMaps);
    }

    protected Optional<String> twoGisUrl(RestroomResponseDto toilet) {
        Map<String, Object> externalMaps = toilet.externalMaps();
        String branchId = extractTwoGisBranchId(externalMaps);
        if (branchId != null && !branchId.isBlank()) {
            return Optional.of(Links.twoGisById(branchId));
        }

        if (toilet.coordinates() == null) {
            return Optional.empty();
        }

        return Optional.of(Links.twoGis(toilet.coordinates().lat(), toilet.coordinates().lon()));
    }

    private Optional<String> mapUrl(RestroomResponseDto toilet, MapLinkBuilder builder) {
        if (toilet.coordinates() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(builder.build(toilet.coordinates().lat(), toilet.coordinates().lon()));
    }

    private String extractTwoGisBranchId(Map<String, Object> externalMaps) {
        if (externalMaps == null) {
            return null;
        }

        Object twoGis = externalMaps.get("2gis");
        if (twoGis instanceof String s) {
            return s;
        }

        if (twoGis instanceof Map<?, ?> twoGisMap) {
            Object branchId = twoGisMap.get("branch_id");
            return branchId instanceof String s ? s : null;
        }

        return null;
    }

    @FunctionalInterface
    private interface MapLinkBuilder {
        String build(double lat, double lon);
    }
}
