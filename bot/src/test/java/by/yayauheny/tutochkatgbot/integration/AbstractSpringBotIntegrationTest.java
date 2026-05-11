package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import yayauheny.by.model.enums.AccessibilityType;
import yayauheny.by.model.enums.DataSourceType;
import yayauheny.by.model.enums.FeeType;
import yayauheny.by.model.enums.ImportProvider;
import yayauheny.by.model.dto.Coordinates;
import yayauheny.by.model.enums.LocationType;
import yayauheny.by.model.restroom.NearestRestroomSlimDto;
import yayauheny.by.model.enums.PlaceType;
import yayauheny.by.model.restroom.RestroomResponseDto;
import yayauheny.by.model.enums.RestroomStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@SpringBootTest(classes = TutochkaTgBotApplication.class, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
public abstract class AbstractSpringBotIntegrationTest {

    protected Update locationUpdate(long chatId, long userId, double lat, double lon) {
        Update update = new Update();
        Message message = new Message();
        message.setChat(chat(chatId));
        message.setMessageId(1);
        message.setFrom(user(userId));
        message.setLocation(Location.builder().latitude(lat).longitude(lon).build());
        update.setMessage(message);
        return update;
    }

    protected Update callbackUpdate(long chatId, long userId, String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(chat(chatId));
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(user(userId));
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    protected Update commandUpdate(long chatId, long userId, String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        message.setChat(chat(chatId));
        message.setMessageId(1);
        message.setFrom(user(userId));
        message.setEntities(
            List.of(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length(text.length())
                    .build()
            )
        );
        update.setMessage(message);
        return update;
    }

    protected RestroomResponseDto restroomResponse(UUID restroomId, kotlinx.serialization.json.JsonObject externalMaps) {
        return new RestroomResponseDto(
            restroomId,
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            new kotlinx.serialization.json.JsonObject(Map.of()),
            new kotlinx.serialization.json.JsonObject(Map.of()),
            FeeType.FREE,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new Coordinates(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            new kotlinx.serialization.json.JsonObject(Map.of()),
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

    protected NearestRestroomSlimDto nearestRestroom(String name, double distanceMeters) {
        return new NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            distanceMeters,
            FeeType.FREE,
            new Coordinates(53.9, 27.56),
            new Coordinates(53.9001, 27.5601)
        );
    }

    protected kotlinx.serialization.json.JsonObject twoGisExternalMap(String branchId) {
        return new kotlinx.serialization.json.JsonObject(
            Map.of("2gis", kotlinx.serialization.json.JsonElementKt.JsonPrimitive(branchId))
        );
    }

    private Chat chat(long id) {
        return Chat.builder().id(id).type("private").build();
    }

    private User user(long id) {
        return User.builder().id(id).isBot(false).firstName("Test").userName("test-user-" + id).build();
    }
}
