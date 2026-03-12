# 🚽 ТуТочка (Tutochka) Backend

> **A REST API for finding public restrooms and toilets worldwide**

[![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/your-org/tutochka-backend/releases/tag/v1.0.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blue.svg)](https://kotlinlang.org/)
[![Ktor](https://img.shields.io/badge/Ktor-3.2.3-green.svg)](https://ktor.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.7-blue.svg)](https://postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 About / О проекте

### English
**TuTochka** is a comprehensive REST API service designed to help people find public restrooms around the world. The service provides location-based search functionality, detailed restroom information including accessibility features, working hours, and amenities.

**Key Features:**
- 🌍 **Global Coverage** - Support for countries and cities worldwide
- 📍 **Location-Based Search** - Find nearest restrooms by coordinates
- 🧭 **Smart Navigation** - Search by venue types (malls, restaurants) with indoor directions inside buildings
- ♿ **Accessibility Info** - Detailed accessibility and amenity information
- 🧩 **Hybrid Data Model** - Relational core (SQL) plus flexible JSONB attributes for rapid data adaptation
- 📱 **RESTful API** - Clean, well-documented REST endpoints
- 🔍 **Advanced Search** - Filter by fee type, accessibility, and more
- 📊 **Pagination** - Efficient data retrieval with pagination support

### Русский
**ТуТочка** — это комплексный REST API сервис, предназначенный для помощи людям в поиске общественных туалетов по всему миру. Сервис предоставляет функциональность поиска на основе местоположения, подробную информацию о туалетах, включая доступность, рабочие часы и удобства.

**Основные возможности:**
- 🌍 **Глобальное покрытие** - Поддержка стран и городов по всему миру
- 📍 **Поиск по местоположению** - Поиск ближайших туалетов по координатам
- 🧭 **Умная навигация** - Поиск по типам заведений (ТЦ, рестораны) и indoor-инструкции внутри зданий
- ♿ **Информация о доступности** - Подробная информация о доступности и удобствах
- 🧩 **Гибридная модель данных** - Жёсткая реляционная структура (SQL) + гибкие атрибуты в JSONB для быстрой адаптации
- 📱 **RESTful API** - Чистые, хорошо документированные REST endpoints
- 🔍 **Расширенный поиск** - Фильтрация по типу оплаты, доступности и другим параметрам
- 📊 **Пагинация** - Эффективное получение данных с поддержкой пагинации

---

## 🛠️ Tech Stack / Технологический стек

### Backend Framework
- **Kotlin 2.1.10** - Modern, concise programming language
- **Ktor 3.2.3** - Asynchronous web framework for Kotlin
- **Koin 3.5.6** - Lightweight dependency injection framework

### Database & ORM
- **PostgreSQL 15+** - Robust, open-source relational database
- **PostGIS** - Spatial database extension for geo queries
- **jOOQ 3.20.8** - Type-safe SQL DSL
- **HikariCP 5.1.0** - High-performance JDBC connection pool

### Development & Quality
- **Liquibase 2.2.2** - Database schema migration tool
- **ktlint** - Kotlin code style checker and formatter
- **JUnit 5** - Testing framework
- **MockK** - Kotlin mocking library
- **Testcontainers** - Integration testing with Docker

### API Documentation
- **OpenAPI 3.0** - API specification and documentation
- **Swagger** - Interactive API documentation

---

## 🚀 Quick Start / Быстрый старт

### Prerequisites / Требования
- **Java 21 LTS**
- **PostgreSQL 15+** с расширениями **postgis**, **pgcrypto**, **btree_gist**
- **Docker** (optional, for containerized setup)

### Environment Variables / Переменные окружения

Create a `.env` file in the project root:

```bash
# Database Configuration / Конфигурация базы данных
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tutochka
DB_USER=your_username
DB_PASSWORD=your_password

# Application Configuration / Конфигурация приложения
APP_PORT=8080
APP_ENV=development
```

### Installation & Running / Установка и запуск

1. **Clone the repository / Клонируйте репозиторий:**
   ```bash
   git clone <repository-url>
   cd tutochka-backend
   ```

2. **Setup Database / Настройте базу данных:**
   ```bash
   # Create database / Создайте базу данных
   createdb tutochka
   
   # Enable PostGIS extension / Включите расширение PostGIS
   psql -d tutochka -c "CREATE EXTENSION IF NOT EXISTS postgis;"
   ```

3. **Run Database Migrations / Запустите миграции базы данных:**
   ```bash
   ./gradlew backend:liquibaseUpdate
   ```

4. **Start the Application / Запустите приложение:**
   ```bash
   # Development mode / Режим разработки
   ./gradlew backend:run
   
   # Or build and run / Или соберите и запустите
   ./gradlew backend:build
   java -jar backend/build/libs/backend-all.jar
   ```

5. **Access the API / Получите доступ к API:**
   - **API Base URL:** `http://localhost:8080/api/v1`
   - **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 📚 API Endpoints / API Endpoints

### Core Endpoints / Основные endpoints

| Method | Endpoint | Description / Описание |
|--------|----------|----------------------|
| `GET` | `/countries` | Get all countries / Получить все страны |
| `GET` | `/cities` | Get all cities / Получить все города |
| `GET` | `/buildings` | List buildings (with `placeType`, `cityId`, JSONB externalIds) |
| `GET` | `/subway/lines` | List subway lines (hexColor, isDeleted) |
| `GET` | `/subway/stations` | List subway stations with geo coordinates |
| `GET` | `/restrooms` | Get all restrooms / Получить все туалеты |
| `GET` | `/restrooms/nearest` | Find nearest restrooms (PostGIS KNN) |
| `GET` | `/restrooms/city/{cityId}` | Get restrooms by city / Получить туалеты по городу |
| `GET` | `/restrooms/{id}` | Get restroom by ID |

### Example Usage / Пример использования

```bash
# Find nearest restrooms / Найти ближайшие туалеты
curl "http://localhost:8080/api/v1/restrooms/nearest?lat=55.7558&lon=37.6176&limit=5"

# Get restrooms in a city / Получить туалеты в городе
curl "http://localhost:8080/api/v1/restrooms/city/123e4567-e89b-12d3-a456-426614174000?page=0&size=10"
```

---

## 🧪 Development / Разработка

### Code Quality / Качество кода

```bash
# Format code / Форматировать код
./gradlew ktlintFormat

# Check code style / Проверить стиль кода
./gradlew ktlintCheck

# Run tests / Запустить тесты
./gradlew test

# Build project / Собрать проект
./gradlew build
```

### Database Management / Управление базой данных

```bash
# Apply migrations / Применить миграции
./gradlew liquibaseUpdate

# Rollback migrations / Откатить миграции
./gradlew liquibaseRollback

# Generate migration / Сгенерировать миграцию
./gradlew liquibaseDiffChangeLog
```

---

## 🐳 Docker Support / Поддержка Docker

```bash
# Build Docker image / Собрать Docker образ
./gradlew buildImage

# Run with Docker Compose / Запустить с Docker Compose
docker-compose up -d
```

---

## 🏗️ Architecture / Архитектура

### Project Structure
- **MVC Pattern:** Clean separation of concerns with controllers, services, and repositories
- **Dependency Injection:** Koin for managing dependencies
- **Database Access:** jOOQ for type-safe SQL queries with PostgreSQL and PostGIS
- **API Layer:** Ktor for building REST APIs with OpenAPI/Swagger documentation
- **Testing:** JUnit 5 + TestContainers for integration tests with PostgreSQL

### Key Components
- **Controllers:** Handle HTTP requests and responses
- **Services:** Implement business logic and orchestrate operations
- **Repositories:** Handle database operations using jOOQ
- **Mappers:** Convert between database records and DTOs
- **Configuration:** Dependency injection modules and database configuration

### Data Model / Модель данных
- **Relational core:** Buildings → Restrooms with subway linkage; cities/countries remain top-level geography.
- **Hybrid attributes:** JSONB columns for amenities, external IDs, and map metadata to iterate quickly without schema churn.
- **Inheritance:** Restrooms can inherit schedules and context from parent buildings (`inherit_building_schedule`).
- **Geo:** PostGIS points for buildings/restrooms and subway stations to enable nearest and indoor-style guidance.

## 🗄️ Database Schema / Схема БД

### English
Core entities: **Buildings** (with `placeType`, `external_ids`, PostGIS coordinates), **Restrooms** (linked to building and subway station, JSONB amenities/external_maps, `placeType`, `fee_type`, `access_note`, `direction_guide`, `inherit_building_schedule`, `has_photos`). Subway lines/stations carry hexColor and soft-delete flags; stations are linked by city/line and used for nearest assignment.

### Русский
Ключевые сущности: **Buildings** (тип места `placeType`, `external_ids`, координаты PostGIS), **Restrooms** (связаны с зданием и станцией метро, JSONB `amenities`/`external_maps`, поля `placeType`, `fee_type`, `access_note`, `direction_guide`, `inherit_building_schedule`, `has_photos`). Линии и станции метро с цветом (hexColor) и soft-delete, станции по городу/линии и используются для привязки ближайшего метро.

---

## 🤖 Telegram Bot / Telegram Бот

### Features / Возможности
- 📍 **Location-based search** — Find nearest restrooms by sharing location
- 🎨 **Clean UI design** — Informative card format with colored subway indicators
- 🔴🔵🟢 **Subway visualization** — Color-coded metro lines for Minsk (Red, Blue, Green)
- 🏢 **Building integration** — Display building names and inherit schedules
- 💸🆓 **Fee indicators** — Clear payment status icons
- ⏰ **Working hours** — Display schedules with inheritance from buildings

### Bot Display Format / Формат отображения бота

**List View (Вариант 2: Карточка с инфострокой):**
```
<b>ТЦ Galleria Minsk</b>
📍 200 м • 🆓 Бесплатно • 🔵 Немига
```

**Detail View:**
- Clean, structured layout with HTML formatting
- Building and subway information
- Working hours (inherited from building if enabled)
- Access notes and direction guides

### Configuration / Конфигурация

See [bot/README.md](bot/README.md) for detailed bot configuration and environment variables.

---

## 🎯 MVP Readiness Checklist / Чеклист готовности MVP

### ✅ Completed / Выполнено
- [x] **Database Schema v1.0** — Complete schema with buildings, subway lines/stations, restrooms
- [x] **REST API** — All core endpoints implemented and tested
- [x] **Telegram Bot** — Functional bot with location-based search
- [x] **Subway Integration** — Metro lines/stations with color coding
- [x] **Building Integration** — Buildings linked to restrooms with schedule inheritance
- [x] **Bot UI Redesign** — Clean, informative display format
- [x] **HTTP Client Resilience** — Retries and timeouts for backend calls
- [x] **Shared DTOs** — Common models between backend and bot modules

### 🔄 In Progress / В процессе
- [ ] **Data Population** — Initial data import for Minsk (buildings, subway, restrooms)
- [ ] **Production Deployment** — Docker setup, environment configuration

### 📋 Remaining for MVP / Осталось для MVP
- [ ] **Production Environment Setup**
  - [ ] Docker Compose configuration for production
  - [ ] Environment variable documentation
  - [ ] Database backup strategy
  - [ ] Monitoring and logging setup
- [ ] **Data Import**
  - [ ] Minsk buildings data (shopping centers, restaurants, etc.)
  - [ ] Minsk subway lines and stations (3 lines: Red, Blue, Green)
  - [ ] Initial restroom data for Minsk
  - [ ] Building-restroom linkages
  - [ ] Subway station-restroom assignments
- [ ] **Testing & Quality**
  - [ ] End-to-end testing
  - [ ] Load testing for API endpoints
  - [ ] Bot user acceptance testing
- [ ] **Documentation**
  - [ ] API usage examples
  - [ ] Deployment guide
  - [ ] Bot user guide

### 🚀 Next Steps for MVP Launch / Следующие шаги для запуска MVP
1. **Data Import** — Populate database with Minsk data (buildings, subway, restrooms)
2. **Production Setup** — Configure Docker, environment variables, monitoring
3. **Testing** — Comprehensive testing of all features
4. **Documentation** — Complete user and deployment documentation
5. **Launch** — Deploy to production environment

## 📖 Documentation / Документация

- **API Documentation:** Available at `/swagger-ui.html` when running
- **Database Schema:** See `src/main/resources/db/changelog/` for migration files
- **Code Style:** Follows ktlint standards with automatic formatting

---


## 📞 Support / Поддержка

- **Email:** support@tutochka.by
- **Issues:** [GitHub Issues](https://github.com/your-org/tutochka-backend/issues)

---

## 🗺️ Roadmap & Future Improvements / Дорожная карта и улучшения

### Phase 1: Automation & Data (Автоматизация и Данные)
- [x] **Schedule Sync** — Inherit restroom schedule from building (`inherit_building_schedule`) ✅ **COMPLETED**
- [x] **Subway Integration** — Import metro lines/stations and geo-search nearest stations ✅ **COMPLETED**
- [x] **Bot Display Redesign** — Clean, informative design with colored subway indicators (🔴🔵🟢) ✅ **COMPLETED**
- [ ] **Automated Import Engine** — Parsers for 2GIS and Yandex Maps to auto-create buildings and attach restrooms.

### Phase 2: Community & Content (Сообщество и Контент)
- [ ] **User Submissions Bot** — Telegram bot to receive new restroom submissions from users.
- [ ] **Admin Moderation Panel** — Approve/reject bot submissions and resolve import conflicts.
- [ ] **Photo Storage** — Integrate S3/MinIO for real photos (replace `has_photos` placeholder).
- [ ] **Review System** — Full reviews and ratings (DB stubs already exist).

### Phase 3: Ecosystem & Monetization (Экосистема и Монетизация)
- [ ] **Access Codes System** — Store/share access codes and key notes (`access_note` already in schema).
- [ ] **Contributor Rewards** — Incentives for users who update data or add new locations.
- [ ] **Advanced Filtering** — JSONB-based filters for amenities (changing tables, showers, etc.).

---

<div align="center">
  <p>Made with ❤️ for better public restroom accessibility</p>
  <p>Сделано с ❤️ для лучшей доступности общественных туалетов</p>
</div>