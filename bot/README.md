# Tutochka Telegram Bot Module

Telegram bot модуль для поиска туалетов на основе геолокации.

## Независимый запуск

Bot модуль может запускаться независимо от backend модуля. Он использует REST API для взаимодействия с backend через `WebBackendClient`.

### Требования

- Java 21+
- Gradle 8.0+
- Telegram Bot Token (опционально для тестирования)

### Переменные окружения

#### Обязательные (для работы бота)
- `TELEGRAM_BOT_TOKEN` - токен Telegram бота
- `TELEGRAM_BOT_USERNAME` - username Telegram бота

#### Опциональные
- `BACKEND_BASE_URL` - URL backend API (по умолчанию: `http://localhost:8080/api/v1`)
- `PORT` - порт для HTTP сервера (по умолчанию: `8081`)
- `BOT_MODE` - режим работы бота: `POLLING` или `WEBHOOK` (по умолчанию: `POLLING`)
- `BOT_AUTO_REGISTER` - автоматическая регистрация бота при старте (по умолчанию: `false`)
- `BOT_WEBHOOK_PATH` - путь для webhook (по умолчанию: `/telegram/webhook`)
- `BOT_WEBHOOK_PUBLIC_URL` - публичный URL для webhook

### Сборка

```bash
# Сборка только bot модуля
./gradlew :bot:build

# Сборка без тестов
./gradlew :bot:build -x test
```

### Запуск

```bash
# Запуск bot модуля
./gradlew :bot:bootRun

# Или с переменными окружения
TELEGRAM_BOT_TOKEN=your_token TELEGRAM_BOT_USERNAME=your_bot ./gradlew :bot:bootRun
```

### Зависимости от backend модуля

Bot модуль зависит от backend модуля только на уровне **компиляции**:
- Использует shared DTOs из `:shared` модуля
- Использует интерфейс `BackendClient` для абстракции вызовов
- Реализация `WebBackendClient` использует REST вызовы

**Важно**: Bot модуль может запускаться без активного backend процесса. Вызовы к backend будут падать с ошибками соединения, но приложение не крашится.

### Конфигурация

Конфигурация находится в `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: tutochka-tg-bot

telegram:
  bot:
    username: ${TELEGRAM_BOT_USERNAME:your_bot_username}
    token: ${TELEGRAM_BOT_TOKEN:your_bot_token}

bot:
  mode: ${BOT_MODE:POLLING}
  auto-register: ${BOT_AUTO_REGISTER:false}
  webhook-path: ${BOT_WEBHOOK_PATH:/telegram/webhook}
  webhook-public-url: ${BOT_WEBHOOK_PUBLIC_URL:https://your-domain.com/telegram/webhook}

backend:
  base-url: ${BACKEND_BASE_URL:http://localhost:8080/api/v1}

server:
  port: ${PORT:8081}
```

### Режимы работы

#### POLLING (по умолчанию)
- Bot периодически опрашивает Telegram API для получения обновлений
- Не требует публичного URL
- Устанавливается через `BOT_MODE=POLLING` или по умолчанию

#### WEBHOOK
- Telegram отправляет обновления на указанный URL
- Требует публичный URL и HTTPS
- Устанавливается через `BOT_MODE=WEBHOOK`
- Требует настройки `BOT_WEBHOOK_PUBLIC_URL`

### Проверка независимого запуска

1. **Сборка**: `./gradlew clean :bot:build -x test` - должна проходить успешно
2. **Запуск**: `./gradlew :bot:bootRun` - приложение должно стартовать без ошибок
3. **Логи**: Должны показывать успешный старт Spring Boot без ошибок инициализации

### Известные ограничения

- Bot модуль требует доступности backend API для полноценной работы
- При отсутствии backend вызовы будут падать с ошибками соединения
- Для тестирования можно использовать mock backend или отключить функциональность, требующую backend
