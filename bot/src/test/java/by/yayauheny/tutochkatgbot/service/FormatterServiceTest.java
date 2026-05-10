package by.yayauheny.tutochkatgbot.service;

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
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import kotlinx.serialization.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatterServiceTest {

    private final FormatterService formatterService = new FormatterService();

    @Test
    void toiletDetail_shouldEscapeHtmlInDynamicContent() {
        RestroomResponseDto restroom =
            new RestroomResponseDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Minsk",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "<b>Bad</b>",
                "Main <script>alert(1)</script> Street",
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
                new JsonObject(Map.of()),
                null,
                "Find it near <b>exit</b>",
                false,
                false,
                LocationType.UNKNOWN,
                ImportProvider.USER,
                null,
                false,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-01T00:00:00Z"),
                120,
                new BuildingResponseDto(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "HQ <script>",
                    "Some <i>address</i>",
                    PlaceType.OTHER,
                    new JsonObject(Map.of()),
                    new Coordinates(53.91, 27.57),
                    new JsonObject(Map.of()),
                    false,
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z")
                ),
                new SubwayStationResponseDto(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Victory <b>Square</b>",
                    "Victory Square",
                    false,
                    new Coordinates(53.92, 27.58),
                    false,
                    Instant.parse("2025-01-01T00:00:00Z"),
                    new SubwayLineResponseDto(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Red",
                        "Red",
                        "1",
                        "#FF0000",
                        false,
                        Instant.parse("2025-01-01T00:00:00Z")
                    )
                )
            );

        String formatted = formatterService.toiletDetail(restroom, 42.0);

        assertThat(formatted).contains("&lt;b&gt;Bad&lt;/b&gt;");
        assertThat(formatted).contains("Main &lt;script&gt;alert(1)&lt;/script&gt; Street");
        assertThat(formatted).contains("Find it near &lt;b&gt;exit&lt;/b&gt;");
        assertThat(formatted).contains("HQ &lt;script&gt;");
        assertThat(formatted).contains("Victory &lt;b&gt;Square&lt;/b&gt;");
        assertThat(formatted).doesNotContain("<script>alert(1)</script>");
        assertThat(formatted).doesNotContain("<b>Bad</b>");
    }
}
