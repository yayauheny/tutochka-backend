package by.yayauheny.tutochkatgbot.handler;

import by.yayauheny.tutochkatgbot.callback.CallbackData;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Context for update processing
 */
public record UpdateContext(
    long chatId,
    long userId,
    String text,
    boolean hasLocation,
    Double latitude,
    Double longitude,
    boolean isCallback,
    String callbackData,
    Integer messageId
) {
    
    /**
     * Create UpdateContext from Telegram update
     * @param update Telegram update
     * @return context with extracted data
     * @throws IllegalArgumentException if message.getFrom() is null (should not happen in normal bot scenarios)
     */
    public static UpdateContext from(Update update) {
        if (update.hasMessage()) {
            return fromMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            return fromCallbackQuery(update.getCallbackQuery());
        } else {
            return new UpdateContext(0, 0, null, false, null, null, false, null, null);
        }
    }
    
    private static UpdateContext fromMessage(org.telegram.telegrambots.meta.api.objects.Message message) {
        long chatId = message.getChatId();
        long userId = extractUserId(message.getFrom());
        String text = message.getText();
        Integer messageId = message.getMessageId();
        
        LocationData locationData = extractLocationData(message);
        
        return new UpdateContext(
            chatId, 
            userId, 
            text, 
            locationData.hasLocation(), 
            locationData.latitude(), 
            locationData.longitude(), 
            false, 
            null, 
            messageId
        );
    }
    
    private static UpdateContext fromCallbackQuery(org.telegram.telegrambots.meta.api.objects.CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = extractUserId(callbackQuery.getFrom());
        String callbackData = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        
        return new UpdateContext(
            chatId, 
            userId, 
            null, 
            false, 
            null, 
            null, 
            true, 
            callbackData, 
            messageId
        );
    }
    
    private static long extractUserId(org.telegram.telegrambots.meta.api.objects.User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null - this should not happen in normal bot scenarios");
        }
        return user.getId();
    }
    
    private static LocationData extractLocationData(org.telegram.telegrambots.meta.api.objects.Message message) {
        if (message.hasLocation()) {
            var location = message.getLocation();
            return new LocationData(true, location.getLatitude(), location.getLongitude());
        } else if (message.getVenue() != null && message.getVenue().getLocation() != null) {
            var venueLocation = message.getVenue().getLocation();
            return new LocationData(true, venueLocation.getLatitude(), venueLocation.getLongitude());
        } else {
            return new LocationData(false, null, null);
        }
    }
    
    private record LocationData(boolean hasLocation, Double latitude, Double longitude) {}
    
    public boolean isTextCommand(String command) {
        return text != null && text.equals(command);
    }
    
    public boolean hasLocation() {
        return hasLocation;
    }
    
    public boolean isCallback() {
        return isCallback;
    }
    
    public boolean isCallbackOfType(String type) {
        return isCallback && CallbackData.isType(callbackData, type);
    }
    
    public String callbackArg() {
        return CallbackData.arg(callbackData);
    }
}
