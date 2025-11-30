package by.yayauheny.tutochkatgbot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Interface for command handlers
 */
public interface CommandHandler {
    
    /**
     * Get the command this handler processes
     * @return command string (e.g., "/start", "/help")
     */
    String command();
    
    /**
     * Check if this handler can process the update
     * @param update Telegram update
     * @return true if this handler can process the update
     */
    boolean canHandle(Update update);
    
    /**
     * Handle the command
     * @param update Telegram update
     * @param ctx update context
     * @throws Exception if handling fails
     */
    void handle(Update update, UpdateContext ctx) throws Exception;
}
