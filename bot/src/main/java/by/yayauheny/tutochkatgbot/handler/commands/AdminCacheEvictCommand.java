package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.config.AdminProperties;
import by.yayauheny.tutochkatgbot.handler.CommandHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.util.CommandUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

@Component
@Order(4)
public class AdminCacheEvictCommand implements CommandHandler {
    private static final String COMMAND_GEO = "/evict-geo";
    private static final String COMMAND_INFO = "/evict-info";

    private final RestroomCacheService cacheService;
    private final MessageSender sender;
    private final Set<Long> adminIds;

    public AdminCacheEvictCommand(RestroomCacheService cacheService, MessageSender sender, AdminProperties adminProperties) {
        this.cacheService = cacheService;
        this.sender = sender;
        this.adminIds = adminProperties.asLongSet();
    }

    @Override
    public String command() {
        return COMMAND_GEO;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        String command = CommandUtils.extractCommand(message);
        if (command == null || !(COMMAND_GEO.equals(command) || COMMAND_INFO.equals(command))) {
            return false;
        }
        long userId = message.getFrom().getId();
        return isAdmin(userId);
    }

    @Override
    public void handle(Update update, UpdateContext ctx) {
        String command = ctx.text();
        if (COMMAND_GEO.equals(command)) {
            cacheService.evictGeo();
            sender.sendText(ctx.chatId(), "Кэш ближайших туалетов очищен.");
            return;
        }

        if (COMMAND_INFO.equals(command)) {
            cacheService.evictInfo();
            sender.sendText(ctx.chatId(), "Кэш информации о туалетах очищен.");
            return;
        }

        sender.sendText(ctx.chatId(), "Неизвестная команда.");
    }

    private boolean isAdmin(long userId) {
        return adminIds.contains(userId);
    }
}
