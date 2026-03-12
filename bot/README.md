# 🤖 Tutochka Telegram Bot
> Telegram bot for finding public restrooms via TuTochka API

[![Version](https://img.shields.io/badge/Version-0.1.0-blue.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.x-blue.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Telegram Bot API](https://img.shields.io/badge/Telegram-Bot_API-179cde.svg)](https://core.telegram.org/bots/api)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

---

## 📖 About / О боте

### English
TuTochka Telegram Bot is a Spring Boot service that sits on top of the TuTochka backend API. It performs location-based restroom search and renders rich cards with distance, fees, nearest subway, working hours, and building-aware details. The bot stays alive even if the backend is down—only the API calls fail gracefully.

**Key features**:
- 📍 **Location-based search** — send your geolocation and get nearest restrooms.
- 🎨 **Rich formatting** — neat cards with HTML, emojis, and links.
- 🚇 **Subway integration** — shows nearest station (line color + name) when available.
- 🏢 **Building-aware results** — inherits schedule/address hints from buildings when present.
- ♿ **Accessibility & fees** — shows paid/free and accessibility flags if backend provides them.
- 🔗 **Decoupled runtime** — bot process keeps running; only backend calls error out.

### Русский
TuTochka Telegram Bot — Spring Boot сервис поверх TuTochka backend API. Делает поиск туалетов по геолокации и показывает аккуратные карточки: расстояние, платность, ближайшее метро, время работы и данные зданий. Даже при недоступности backend бот не падает — ошибки возникают только на запросах к API.

**Основные возможности**:
- 📍 **Поиск по геолокации** — отправляете геопозицию, получаете ближайшие туалеты.
- 🎨 **Красивые карточки** — HTML-разметка, эмодзи, удобные ссылки.
- 🚇 **Интеграция с метро** — ближайшая станция с цветом линии (если есть в ответе).
- 🏢 **Учёт зданий** — адрес и график могут наследоваться от здания.
- ♿ **Доступность и платность** — показываются, если backend их отдаёт.
- 🔗 **Не зависит от аптайма backend** — при ошибке API падают только запросы, процесс бота живёт.

---

## 🏗️ Architecture & Integration / Архитектура и интеграция
- Модуль `:bot` — отдельное Spring Boot приложение внутри монорепо.
- Общие DTO берутся из `:shared` модуля, поэтому ответы от backend десериализуются без дублирования моделей.
- Коммуникация с backend через интерфейс `BackendClient`; реализация `WebBackendClient` ходит по HTTP к `BACKEND_BASE_URL`.
- Типовые сценарии:
  - Пользователь отправляет локацию → бот вызывает `GET /restrooms/nearest` → возвращает список карточек с расстоянием, метро, платностью и графиком.
  - Пользователь вводит `/start` → бот отвечает приветствием и просит отправить геопозицию.

**Поток данных**: Telegram User ↔ Bot (Spring Boot) ↔ Backend API (TuTochka) ↔ DB/PostGIS.

---

## 🛠️ Tech Stack / Технологический стек
- **Kotlin** (JVM 21)
- **Spring Boot** (web, configuration)
- **Telegram Bot API** client library (see `bot` module build for exact artifact)
- **HTTP client** for backend integration (`WebBackendClient`)
- **Logback** for logging

---

## 🚀 Quick Start / Быстрый старт

### Prerequisites / Требования
- Java 21+
- Gradle 8+
- Созданный бот в Telegram (BotFather: создать, получить token и username)

### Environment Variables / Переменные окружения
Обязательные для работы бота:
- `TELEGRAM_BOT_TOKEN` — токен Telegram бота
- `TELEGRAM_BOT_USERNAME` — username Telegram бота

Опциональные:
- `BACKEND_BASE_URL` — URL backend API (по умолчанию `http://localhost:8080/api/v1`)
- `BACKEND_CONNECT_TIMEOUT_MS` — таймаут подключения, мс (по умолчанию `3000`)
- `BACKEND_READ_TIMEOUT_MS` — таймаут чтения, мс (по умолчанию `5000`)
- `BACKEND_RETRY_ATTEMPTS` — число повторов (по умолчанию `2`)
- `BACKEND_RETRY_DELAY_MS` — задержка между повторами, мс (по умолчанию `200`)
- `PORT` — HTTP порт бота (по умолчанию `8081`)
- `BOT_MODE` — `POLLING` или `WEBHOOK` (по умолчанию `POLLING`)
- `BOT_AUTO_REGISTER` — авто-регистрация бота (по умолчанию `false`)
- `BOT_WEBHOOK_PATH` — путь webhook (по умолчанию `/telegram/webhook`)
- `BOT_WEBHOOK_PUBLIC_URL` — публичный URL для webhook

### Build & Run / Сборка и запуск
```bash
# Сборка только bot модуля
./gradlew :bot:build

# Сборка без тестов
./gradlew :bot:build -x test

# Запуск бота
./gradlew :bot:bootRun

# Запуск с переменными окружения
TELEGRAM_BOT_TOKEN=your_token TELEGRAM_BOT_USERNAME=your_bot ./gradlew :bot:bootRun
```

Бот можно запускать независимо от backend процесса: он использует `WebBackendClient` для REST-запросов; при недоступности backend запросы вернут ошибки соединения, но приложение останется живым.

---

## ⚙️ Configuration / Конфигурация
Основная конфигурация — `src/main/resources/application.yml`:
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
  connect-timeout-ms: ${BACKEND_CONNECT_TIMEOUT_MS:3000}
  read-timeout-ms: ${BACKEND_READ_TIMEOUT_MS:5000}
  retry-attempts: ${BACKEND_RETRY_ATTEMPTS:2}
  retry-delay-ms: ${BACKEND_RETRY_DELAY_MS:200}

server:
  port: ${PORT:8081}
```

---

## 🔄 Modes / Режимы работы
### POLLING (default)
- Бот периодически опрашивает Telegram API.
- Не требует публичного URL.
- Выставляется через `BOT_MODE=POLLING` или по умолчанию.

### WEBHOOK
- Telegram присылает обновления на ваш URL.
- Требуется публичный HTTPS URL и корректный `BOT_WEBHOOK_PUBLIC_URL`.
- Подходит для прод окружений с доступным доменом.

---

## 🧭 Roadmap
- Улучшенный UX: инлайн-кнопки для фильтров (платный/бесплатный, доступность).
- Команды `/help` и `/settings` с выбором языка/режима поиска.
- Кэширование ответов backend для популярных локаций.
- Более подробные ошибки и ретраи при недоступности backend.
- Поддержка удобной настройки webhook (сертификаты, авто-регистрация).

---

## 🛠 Troubleshooting / Частые проблемы
- **Invalid token / username** — проверьте `TELEGRAM_BOT_TOKEN` и `TELEGRAM_BOT_USERNAME` (BotFather).
- **Backend unreachable** — убедитесь в корректности `BACKEND_BASE_URL`, доступности порта и сетевых правил.
- **Webhook not receiving updates** — проверьте `BOT_WEBHOOK_PUBLIC_URL`, что URL HTTPS-доступен снаружи, и что `BOT_MODE=WEBHOOK`.
- **Port already in use** — измените `PORT` или освободите порт 8081.

---

## ✅ Independent Run Checklist / Проверка независимого запуска
1. `./gradlew clean :bot:build -x test` — сборка проходит.
2. `./gradlew :bot:bootRun` — приложение стартует без ошибок.
3. Логи подтверждают успешный старт Spring Boot; при недоступном backend видны только ошибки вызовов API, без падения процесса.
