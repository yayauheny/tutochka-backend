package by.yayauheny.tutochkatgbot.keyboard;

import by.yayauheny.tutochkatgbot.dto.backend.AccessibilityType;
import by.yayauheny.tutochkatgbot.dto.backend.BuildingResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.DataSourceType;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.ImportProvider;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.LocationType;
import by.yayauheny.tutochkatgbot.dto.backend.PlaceType;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomStatus;
import by.yayauheny.tutochkatgbot.dto.backend.SubwayLineResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.SubwayStationResponseDto;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.util.Links;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static org.assertj.core.api.Assertions.assertThat;

class InlineKeyboardFactoryTest {
    private final InlineKeyboardFactory factory = new InlineKeyboardFactory(new FormatterService());

    @Test
    void toiletDetailShouldRenderUrlButtonsWithSameLayout() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroom(Map.of()));

        assertThat(keyboard.getKeyboard()).hasSize(3);
        assertThat(keyboard.getKeyboard().get(0)).hasSize(2);
        assertThat(keyboard.getKeyboard().get(1)).hasSize(2);
        assertThat(keyboard.getKeyboard().get(2)).hasSize(1);

        assertMapButton(keyboard.getKeyboard().get(0).get(0), "🗺 Яндекс Карты");
        assertMapButton(keyboard.getKeyboard().get(0).get(1), "🗺 Google Maps");
        assertMapButton(keyboard.getKeyboard().get(1).get(0), "🗺 2ГИС");
        assertMapButton(keyboard.getKeyboard().get(1).get(1), "🗺 Apple Maps");
        assertThat(keyboard.getKeyboard().get(2).get(0).getText()).isEqualTo("← Назад");
        assertThat(keyboard.getKeyboard().get(2).get(0).getCallbackData()).isEqualTo("back:list");
    }

    @Test
    void toiletDetailShouldSkipMapButtonsWithoutUrl() {
        InlineKeyboardFactory partialFactory =
            new InlineKeyboardFactory(new FormatterService()) {
                @Override
                protected java.util.Optional<String> appleUrl(RestroomResponseDto toilet) {
                    return java.util.Optional.empty();
                }
            };

        InlineKeyboardMarkup keyboard = partialFactory.toiletDetail(sampleRestroom(Map.of()));

        assertThat(keyboard.getKeyboard()).hasSize(3);
        assertThat(keyboard.getKeyboard().get(0)).hasSize(2);
        assertThat(keyboard.getKeyboard().get(1)).hasSize(1);
        assertThat(keyboard.getKeyboard().get(1).get(0).getText()).isEqualTo("🗺 2ГИС");
        assertThat(keyboard.getKeyboard().get(1).get(0).getUrl()).isNotBlank();
        assertThat(keyboard.getKeyboard().get(1).get(0).getCallbackData()).isNull();
    }

    @Test
    void twoGisButtonShouldUseBranchIdWhenAvailable() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroom(Map.of(
            "2gis", (Object) "abc123"
        )));

        InlineKeyboardButton twoGisButton = keyboard.getKeyboard().get(1).get(0);

        assertThat(twoGisButton.getText()).isEqualTo("🗺 2ГИС");
        assertThat(twoGisButton.getUrl()).isEqualTo(Links.twoGisById("abc123"));
        assertThat(twoGisButton.getCallbackData()).isNull();
    }

    @Test
    void twoGisButtonShouldUseBranchIdEvenWithoutCoordinates() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroomWithoutCoordinates(Map.of(
            "2gis", (Object) "abc123"
        )));

        assertThat(keyboard.getKeyboard()).hasSize(2);
        assertThat(keyboard.getKeyboard().get(0)).hasSize(1);

        InlineKeyboardButton twoGisButton = keyboard.getKeyboard().get(0).get(0);

        assertThat(twoGisButton.getText()).isEqualTo("🗺 2ГИС");
        assertThat(twoGisButton.getUrl()).isEqualTo(Links.twoGisById("abc123"));
        assertThat(twoGisButton.getCallbackData()).isNull();
    }

    @Test
    void twoGisButtonShouldFallbackToGeoUrlWithoutBranchId() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroom(Map.of()));

        InlineKeyboardButton twoGisButton = keyboard.getKeyboard().get(1).get(0);

        assertThat(twoGisButton.getUrl()).isEqualTo(Links.twoGis(53.9, 27.56));
        assertThat(twoGisButton.getCallbackData()).isNull();
    }

    private void assertMapButton(InlineKeyboardButton button, String expectedText) {
        assertThat(button.getText()).isEqualTo(expectedText);
        assertThat(button.getUrl()).isNotBlank();
        assertThat(button.getCallbackData()).isNull();
    }

    private RestroomResponseDto sampleRestroom(Map<String, Object> externalMaps) {
        return new RestroomResponseDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            Map.of(),
            Map.of(),
            FeeType.UNKNOWN,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new LatLon(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            Map.of(),
            externalMaps,
            null,
            null,
            false,
            false,
            LocationType.UNKNOWN,
            ImportProvider.USER,
            null,
            false,
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-01-01T00:00:00Z"),
            100,
            null,
            null
        );
    }

    private RestroomResponseDto sampleRestroomWithoutCoordinates(Map<String, Object> externalMaps) {
        return new RestroomResponseDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            Map.of(),
            Map.of(),
            FeeType.UNKNOWN,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            null,
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            Map.of(),
            externalMaps,
            null,
            null,
            false,
            false,
            LocationType.UNKNOWN,
            ImportProvider.USER,
            null,
            false,
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-01-01T00:00:00Z"),
            100,
            null,
            null
        );
    }
}
