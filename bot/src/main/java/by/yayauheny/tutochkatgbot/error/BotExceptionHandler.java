package by.yayauheny.tutochkatgbot.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class BotExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BotExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BotErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<BotErrorResponse> handleRestClient(RestClientResponseException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return respond(status, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BotErrorResponse> handleAny(Exception ex) {
        log.error("Unhandled bot exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new BotErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "что-то пошло не так"));
    }

    private ResponseEntity<BotErrorResponse> respond(HttpStatus status, Exception ex) {
        log.error("Unhandled bot exception: status={}, message={}", status.value(), ex.getMessage(), ex);
        return ResponseEntity.status(status).body(new BotErrorResponse(status.value(), ex.getMessage()));
    }
}
