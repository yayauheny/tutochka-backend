package by.yayauheny.tutochkatgbot.util;

import org.telegram.telegrambots.meta.api.objects.message.Message;
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

        String text = extractMessageText(message);
        if (text == null) {
            return null;
        }

        List<MessageEntity> entities = extractEntities(message);
        if (entities == null) {
            return null;
        }

        for (MessageEntity entity : entities) {
            if ("bot_command".equals(entity.getType())) {
                int offset = entity.getOffset();
                int length = entity.getLength();
                if (offset >= 0 && offset + length <= text.length()) {
                    String command = text.substring(offset, offset + length);
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

    public static String extractCommandArgument(Message message) {
        if (message == null) {
            return null;
        }

        String text = extractMessageText(message);
        List<MessageEntity> entities = extractEntities(message);
        if (text == null || entities == null) {
            return null;
        }

        for (MessageEntity entity : entities) {
            if (!"bot_command".equals(entity.getType())) {
                continue;
            }

            int commandEnd = entity.getOffset() + entity.getLength();
            if (commandEnd >= text.length()) {
                return null;
            }

            String argument = text.substring(commandEnd).trim();
            return argument.isBlank() ? null : argument;
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

        List<MessageEntity> entities = extractEntities(message);
        if (entities != null) {
            for (MessageEntity entity : entities) {
                if ("bot_command".equals(entity.getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String extractMessageText(Message message) {
        String text = message.getText();
        if (text == null) {
            text = message.getCaption();
        }
        return text;
    }

    private static List<MessageEntity> extractEntities(Message message) {
        List<MessageEntity> entities = message.getEntities();
        if (entities == null) {
            entities = message.getCaptionEntities();
        }
        return entities;
    }
}
