package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.config.BackendProperties;
import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Web client implementation for backend integration
 */
@Component
public class WebBackendClient implements BackendClient {
    private final RestClient client;

    public WebBackendClient(BackendProperties props) {
        this.client = RestClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    @Override
    public List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit) {
        NearestRestroomResponseDto[] array = client.get()
            .uri(uri -> uri.path("/restrooms/nearest")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("limit", limit)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(NearestRestroomResponseDto[].class);
        return array == null ? List.of() : Arrays.asList(array);
    }

    @Override
    public Optional<RestroomResponseDto> getById(String id) {
        RestroomResponseDto dto = client.get()
            .uri("/restrooms/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(RestroomResponseDto.class);
        return Optional.ofNullable(dto);
    }
}
