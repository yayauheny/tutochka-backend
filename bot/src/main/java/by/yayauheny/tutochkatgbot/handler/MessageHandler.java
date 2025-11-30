package by.yayauheny.tutochkatgbot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Interface for message handlers (location, text, etc.)
 */
public interface MessageHandler {
    
    /**
     * Check if this handler can process the message
     * @param update Telegram update
     * @param ctx update context
     * @return true if this handler can process the message
     */
    boolean canHandle(Update update, UpdateContext ctx);
    
    /**
     * Handle the message
     * @param update Telegram update
     * @param ctx update context
     * @throws Exception if handling fails
     */
    void handle(Update update, UpdateContext ctx) throws Exception;
}
