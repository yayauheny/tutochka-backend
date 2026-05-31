# Migration Inventory

## Old bot production classes

- `bot/src/main/java/by/yayauheny/tutochkatgbot/ingress/WebhookController.java`: Telegram webhook entry point. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/ingress/TelegramWebhookSecretTokenFilter.java`: Webhook secret verification. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/router/UpdateRouter.java`: Telegram update parsing/routing. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/commands/StartCommand.java`: `/start` or startup command behavior. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/messages/PlainTextFallbackMessageHandler.java`: text message handling. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/messages/LocationMessageHandler.java`: location message handling. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/callbacks/*.java`: callback query handling. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/SearchService.java`: nearest restroom search flow. Status: to migrate to backend/replace with backend service.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/FormatterService.java`: result/details/route formatting. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/error/BotExceptionHandler.java`: error/fallback response. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/metrics/BotMetrics.java`: analytics event emission. Status: to migrate to backend/replace with backend analytics.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/service/UserService.java`: Telegram user/chat metadata extraction. Status: replace with backend User creation/encryption logic.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/util/TextUtil.java`, `BotSender.java`: logging/sanitization behavior. Status: to migrate to backend.
- `bot/src/main/java/by/yayauheny/tutochkatgbot/config/TelegramProperties.java`: bot token/config usage. Status: to migrate to backend config.

