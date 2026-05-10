package by.yayauheny.tutochkatgbot.keyboard;

import yayauheny.by.model.enums.AccessibilityType;
import yayauheny.by.model.building.BuildingResponseDto;
import yayauheny.by.model.enums.DataSourceType;
import yayauheny.by.model.enums.FeeType;
import yayauheny.by.model.enums.ImportProvider;
import yayauheny.by.model.dto.Coordinates;
import yayauheny.by.model.enums.LocationType;
import yayauheny.by.model.enums.PlaceType;
import yayauheny.by.model.restroom.RestroomResponseDto;
import yayauheny.by.model.enums.RestroomStatus;
import yayauheny.by.model.subway.SubwayLineResponseDto;
import yayauheny.by.model.subway.SubwayStationResponseDto;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.util.Links;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static org.assertj.core.api.Assertions.assertThat;

class InlineKeyboardFactoryTest {
    private final InlineKeyboardFactory factory = new InlineKeyboardFactory(new FormatterService());

    @Test
    void toiletDetailShouldRenderUrlButtonsWithSameLayout() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroom(new JsonObject(Map.of())));

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

        InlineKeyboardMarkup keyboard = partialFactory.toiletDetail(sampleRestroom(new JsonObject(Map.of())));

        assertThat(keyboard.getKeyboard()).hasSize(3);
        assertThat(keyboard.getKeyboard().get(0)).hasSize(2);
        assertThat(keyboard.getKeyboard().get(1)).hasSize(1);
        assertThat(keyboard.getKeyboard().get(1).get(0).getText()).isEqualTo("🗺 2ГИС");
        assertThat(keyboard.getKeyboard().get(1).get(0).getUrl()).isNotBlank();
        assertThat(keyboard.getKeyboard().get(1).get(0).getCallbackData()).isNull();
    }

    @Test
    void twoGisButtonShouldUseBranchIdWhenAvailable() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroom(new JsonObject(Map.of(
            "2gis", JsonElementKt.JsonPrimitive("abc123")
        ))));

        InlineKeyboardButton twoGisButton = keyboard.getKeyboard().get(1).get(0);

        assertThat(twoGisButton.getText()).isEqualTo("🗺 2ГИС");
        assertThat(twoGisButton.getUrl()).isEqualTo(Links.twoGisById("abc123"));
        assertThat(twoGisButton.getCallbackData()).isNull();
    }

    @Test
    void twoGisButtonShouldUseBranchIdEvenWithoutCoordinates() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroomWithoutCoordinates(new JsonObject(Map.of(
            "2gis", JsonElementKt.JsonPrimitive("abc123")
        ))));

        assertThat(keyboard.getKeyboard()).hasSize(3);
        assertThat(keyboard.getKeyboard().get(1)).hasSize(2);

        InlineKeyboardButton twoGisButton = keyboard.getKeyboard().get(1).get(0);

        assertThat(twoGisButton.getText()).isEqualTo("🗺 2ГИС");
        assertThat(twoGisButton.getUrl()).isEqualTo(Links.twoGisById("abc123"));
        assertThat(twoGisButton.getCallbackData()).isNull();
    }

    @Test
    void twoGisButtonShouldFallbackToGeoUrlWithoutBranchId() {
        InlineKeyboardMarkup keyboard = factory.toiletDetail(sampleRestroom(new JsonObject(Map.of())));

        InlineKeyboardButton twoGisButton = keyboard.getKeyboard().get(1).get(0);

        assertThat(twoGisButton.getUrl()).isEqualTo(Links.twoGis(53.9, 27.56));
        assertThat(twoGisButton.getCallbackData()).isNull();
    }

    private void assertMapButton(InlineKeyboardButton button, String expectedText) {
        assertThat(button.getText()).isEqualTo(expectedText);
        assertThat(button.getUrl()).isNotBlank();
        assertThat(button.getCallbackData()).isNull();
    }

    private RestroomResponseDto sampleRestroom(JsonObject externalMaps) {
        return new RestroomResponseDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            new JsonObject(Map.of()),
            new JsonObject(Map.of()),
            FeeType.UNKNOWN,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new Coordinates(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            new JsonObject(Map.of()),
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

    private RestroomResponseDto sampleRestroomWithoutCoordinates(JsonObject externalMaps) {
        return new RestroomResponseDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            new JsonObject(Map.of()),
            new JsonObject(Map.of()),
            FeeType.UNKNOWN,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new Coordinates(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            new JsonObject(Map.of()),
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
