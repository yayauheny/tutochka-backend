# Bot Backend Migration Gaps

## Open

- Webhook endpoint delegates to stub `BotUpdateService`. Need to migrate real update router and handlers.
- Analytics and Metrics need to use backend metrics facade instead of old Prometheus registry.
- User creation logic needs to use backend JPA repositories and models.
- Need to port formatting and search logic.
- Telegram message sending (via `TelegramClient`) needs to be wired correctly.

## Fixed

- Moved bot configuration to `TelegramBotProperties` and loaded via `@ConfigurationPropertiesScan`.
- Transferred webhook token validation filter `TelegramWebhookSecretTokenFilter` to backend.
- Migrated webhook entrypoint `/telegram/webhook` to `TelegramWebhookController`.
