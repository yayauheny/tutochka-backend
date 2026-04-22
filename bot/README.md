# Tutochka Telegram Bot

Spring Boot бот поверх TuTochka backend API. Работает через webhook и делает HTTP-запросы в backend.

## Запуск
```bash
./gradlew :bot:bootRun
```

## Обязательные переменные
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`

## Переменные по умолчанию
- `BACKEND_BASE_URL=http://localhost:8080/api/v1`
- `BACKEND_CONNECT_TIMEOUT_MS=3000`
- `BACKEND_READ_TIMEOUT_MS=5000`
- `BACKEND_RETRY_ATTEMPTS=1`
- `BACKEND_RETRY_DELAY_MS=200`
- `BOT_WEBHOOK_PATH=/telegram/webhook`
- `BOT_WEBHOOK_PUBLIC_URL=`
- `BOT_ASYNC_PROCESSING=false`
- `BOT_ADMIN_IDS=`
- `BOT_PORT` / `PORT=8081`

## Поведение
- Polling не используется.
- Webhook регистрируется только если задан `BOT_WEBHOOK_PUBLIC_URL`.
- Если backend недоступен, бот продолжает работать, но запросы к API падают.

## Конфигурация
Источник правды - `bot/src/main/resources/application.yml` и `bot/src/main/resources/application-prod.yml`.
