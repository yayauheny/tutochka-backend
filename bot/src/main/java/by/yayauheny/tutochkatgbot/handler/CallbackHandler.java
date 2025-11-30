package by.yayauheny.tutochkatgbot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Interface for callback query handlers
 */
public interface CallbackHandler {
    
    /**
     * Get the callback prefix this handler processes
     * @return callback prefix (e.g., "t", "b", "r")
     */
    String prefix();
    
    /**
     * Check if this handler can process the callback data
     * @param callbackData callback data string
     * @return true if this handler can process the callback
     */
    boolean canHandle(String callbackData);
    
    /**
     * Handle the callback query
     * @param update Telegram update
     * @param ctx update context
     * @throws Exception if handling fails
     */
    void handle(Update update, UpdateContext ctx) throws Exception;
}
