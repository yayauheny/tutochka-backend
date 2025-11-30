package by.yayauheny.tutochkatgbot.router;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.service.*;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;

/**
 * Unified context for bot handlers containing all necessary dependencies
 * This simplifies handler implementation and makes it easier to add new features
 */
public record BotContext(
    UpdateContext ctx,
    MessageSender out,
    UserService users,
    SearchService search,
    FormatterService fmt
) {}
