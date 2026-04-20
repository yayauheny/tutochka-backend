package by.yayauheny.tutochkatgbot.service;

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
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
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
                Map.of(),
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
                    Map.of(),
                    new LatLon(53.91, 27.57),
                    Map.of(),
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
                    new LatLon(53.92, 27.58),
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
