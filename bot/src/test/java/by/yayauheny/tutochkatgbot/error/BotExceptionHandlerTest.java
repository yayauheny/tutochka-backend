package by.yayauheny.tutochkatgbot.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotExceptionHandlerTest {
    private final BotExceptionHandler handler = new BotExceptionHandler();

    @Test
    void handleIllegalArgumentShouldReturnBadRequest() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad request"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertEquals(400, response.getBody().status());
        assertEquals("bad request", response.getBody().message());
    }

    @Test
    void handleRestClientShouldPreserveStatusAndMessage() {
        var response = handler.handleRestClient(new HttpClientErrorException(HttpStatus.NOT_FOUND, "missing"));

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertEquals(404, response.getBody().status());
        assertTrue(response.getBody().message().contains("missing"));
    }

    @Test
    void handleAnyShouldReturnInternalServerError() {
        var response = handler.handleAny(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals(500, response.getBody().status());
        assertEquals("что-то пошло не так", response.getBody().message());
    }
}
