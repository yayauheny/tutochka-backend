package by.yayauheny.tutochkatgbot.util;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.List;

/**
 * Utility class for extracting commands from Telegram messages
 */
public class CommandUtils {
    
    /**
     * Extract command from message using MessageEntity
     * Correctly handles cases like /start@YourBot and commands in captions
     * @param message Telegram message
     * @return command string (e.g., "/start") or null if not found
     */
    public static String extractCommand(Message message) {
        if (message == null) {
            return null;
        }
        
        String text = message.getText();
        if (text == null) {
            text = message.getCaption();
        }
        
        if (text == null) {
            return null;
        }
        
        List<MessageEntity> entities = message.getEntities();
        if (entities == null) {
            entities = message.getCaptionEntities();
        }
        
        if (entities == null) {
            return null;
        }
        
        for (MessageEntity entity : entities) {
            if ("bot_command".equals(entity.getType())) {
                int offset = entity.getOffset();
                int length = entity.getLength();
                if (offset >= 0 && offset + length <= text.length()) {
                    String command = text.substring(offset, offset + length);
                    // Remove bot username if present (e.g., "/start@YourBot" -> "/start")
                    int atIndex = command.indexOf('@');
                    if (atIndex > 0) {
                        command = command.substring(0, atIndex);
                    }
                    return command;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if message contains a bot command using MessageEntity
     * @param message Telegram message
     * @return true if message contains a bot command
     */
    public static boolean isCommand(Message message) {
        if (message == null) {
            return false;
        }
        
        // Check entities in message text
        if (message.getEntities() != null) {
            for (MessageEntity entity : message.getEntities()) {
                if ("bot_command".equals(entity.getType())) {
                    return true;
                }
            }
        }
        
        // Check entities in caption (for photos, documents, etc.)
        if (message.getCaptionEntities() != null) {
            for (MessageEntity entity : message.getCaptionEntities()) {
                if ("bot_command".equals(entity.getType())) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
