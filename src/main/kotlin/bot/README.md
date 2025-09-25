# Hello Bot

Простой Telegram бот для поиска туалетов, созданный с использованием tgbotapi 28.0.2.

## Функциональность

- `/start` - приветствие и предложение поделиться геолокацией
- `/help` - справка по использованию бота
- Кнопка "📍 Поделиться геолокацией" для запроса местоположения

## Запуск

1. Установите переменную окружения `BOT_TOKEN` с токеном вашего бота:
   ```bash
   export BOT_TOKEN="your_bot_token_here"
   ```

2. Запустите бота:
   ```bash
   ./gradlew run
   ```

   Или запустите напрямую:
   ```bash
   java -cp build/libs/tutochka-backend-1.0-SNAPSHOT.jar yayauheny.by.bot.HelloBotKt
   ```

## Структура проекта

- `HelloBot.kt` - основной файл бота
- `messages/Messages.kt` - сообщения и константы бота
- `keyboards/` - фабрика клавиатур (пустая)
- `handlers/` - обработчики сообщений (пустая)
- `routing/` - маршрутизация (пустая)
- `session/` - управление сессиями (пустая)

## Технологии

- Kotlin
- tgbotapi 28.0.2
- Kotlin Coroutines
- SLF4J для логирования