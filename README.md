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
- **PostgreSQL** - Robust, open-source relational database
- **jOOQ 3.20.8** - Kotlin SQL framework
- **PostGIS** - Spatial database extension for location data
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
- **Java 17+** / **JDK 17+**
- **PostgreSQL 13+** with PostGIS extension
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
   ./gradlew liquibaseUpdate
   ```

4. **Start the Application / Запустите приложение:**
   ```bash
   # Development mode / Режим разработки
   ./gradlew run
   
   # Or build and run / Или соберите и запустите
   ./gradlew build
   java -jar build/libs/tutochka-backend-0.1.0.jar
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
| `GET` | `/countries/{id}` | Get country by ID / Получить страну по ID |
| `GET` | `/cities` | Get all cities / Получить все города |
| `GET` | `/cities/{id}` | Get city by ID / Получить город по ID |
| `GET` | `/restrooms` | Get all restrooms / Получить все туалеты |
| `GET` | `/restrooms/nearest` | Find nearest restrooms / Найти ближайшие туалеты |
| `GET` | `/restrooms/city/{cityId}` | Get restrooms by city / Получить туалеты по городу |

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
Core entities are **Buildings** and **Restrooms**. Restrooms can inherit properties (e.g., schedules) from Buildings. Subway stations are linked geospatially to support navigation and proximity search.

### Русский
Ключевые сущности — **Buildings** и **Restrooms**. Туалеты могут наследовать свойства (например, график работы) от зданий. Станции метро связаны геопространственно для навигации и поиска ближайших точек.

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
- [ ] **Automated Import Engine** — Parsers for 2GIS and Yandex Maps to auto-create buildings and attach restrooms.
- [ ] **Schedule Sync** — Inherit restroom schedule from building (`inherit_building_schedule`).
- [ ] **Subway Integration** — Import metro lines/stations and geo-search nearest stations.

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