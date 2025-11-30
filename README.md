# 🚽 ТуТочка (Tutochka) Backend

> **A REST API for finding public restrooms and toilets worldwide**

[![Version](https://img.shields.io/badge/Version-0.1.0-blue.svg)](https://github.com/your-org/tutochka-backend/releases/tag/v0.1.0)
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
- ♿ **Accessibility Info** - Detailed accessibility and amenity information
- 📱 **RESTful API** - Clean, well-documented REST endpoints
- 🔍 **Advanced Search** - Filter by fee type, accessibility, and more
- 📊 **Pagination** - Efficient data retrieval with pagination support

### Русский
**ТуТочка** — это комплексный REST API сервис, предназначенный для помощи людям в поиске общественных туалетов по всему миру. Сервис предоставляет функциональность поиска на основе местоположения, подробную информацию о туалетах, включая доступность, рабочие часы и удобства.

**Основные возможности:**
- 🌍 **Глобальное покрытие** - Поддержка стран и городов по всему миру
- 📍 **Поиск по местоположению** - Поиск ближайших туалетов по координатам
- ♿ **Информация о доступности** - Подробная информация о доступности и удобствах
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

### High Priority / Высокий приоритет

#### Performance & Scalability / Производительность и масштабируемость
- [ ] **In-memory caching** - Implement Caffeine cache for countries and cities to reduce database load (Task #5)
- [ ] **API optimization** - Batch write operations to Task Master AI to reduce API calls (Task #27)
- [ ] **Query optimization** - Review and optimize database queries, especially for nearest restrooms endpoint

#### Code Quality & Maintainability / Качество кода и поддерживаемость
- [ ] **Standardize error messages** - Centralize error messages and implement i18n-ready constants (Task #8)
- [ ] **Structured logging** - Add context (request path, method, status, query params) to error logs (Task #14)
- [ ] **Update repository exception tests** - Complete tests for RepositoryException and EntityNotFoundException (Task #13.1)

### Medium Priority / Средний приоритет

#### Testing & Quality Assurance / Тестирование и качество
- [ ] **Test quality audit** - Assess test code for readability, duplication, complexity (Task #58)
- [ ] **Test performance analysis** - Identify slow tests and bottlenecks (Task #59)
- [ ] **Test stability** - Detect and fix flaky tests, timing dependencies (Task #60)
- [ ] **Missing test scenarios** - Identify gaps in error handling, edge cases, integration scenarios (Task #61)
- [ ] **Mock usage review** - Evaluate mock patterns and test data creation (Task #62)
- [ ] **Edge case tests** - Add comprehensive tests for validation, pagination, coordinates (Task #47)

#### Architecture & Refactoring / Архитектура и рефакторинг
- [ ] **Package reorganization** - Separate DTOs and entities in model package (Task #15)
- [ ] **Split common package** - Organize into api.errors, mapper, query.builder subpackages (Task #16)
- [ ] **Group utility functions** - Organize util package by domain (http, geo, db, serialization, config) (Task #17)

#### Features / Функциональность
- [ ] **Filtering and status checking** - Implement local task filtering by status, priority, dependencies (Task #29)
- [ ] **AI model integration** - Configure economical models (sonar, gemini-2.0-flash) for operations (Task #28)

### Low Priority / Низкий приоритет

#### Development Tools / Инструменты разработки
- [ ] **Structured logging format** - Enforce markdown format for implementation logs (Task #75)
- [ ] **Commit message format** - Standardize git commit messages (Task #76)
- [ ] **Utility tests** - Add unit tests for GeoDsl, ApplicationCallExtensions, RepositoryExtensions (Task #20)

#### Documentation / Документация
- [ ] **API documentation improvements** - Enhance OpenAPI specs with more examples
- [ ] **Architecture documentation** - Expand ARCHITECTURE_IMPROVEMENTS.md with detailed diagrams
- [ ] **Contributing guide** - Add CONTRIBUTING.md with development guidelines

### Future Considerations / Будущие улучшения

- [ ] **Authentication & Authorization** - Add JWT-based authentication for protected endpoints
- [ ] **Rate limiting** - Implement rate limiting to prevent abuse
- [ ] **Metrics & Monitoring** - Add Prometheus metrics and health checks
- [ ] **CI/CD Pipeline** - Set up GitHub Actions for automated testing and deployment
- [ ] **API versioning** - Implement proper API versioning strategy
- [ ] **GraphQL support** - Consider adding GraphQL endpoint alongside REST
- [ ] **Full-text search** - Add Elasticsearch for advanced search capabilities
- [ ] **Image upload** - Support for restroom photos and images
- [ ] **User reviews & ratings** - Allow users to rate and review restrooms
- [ ] **Real-time updates** - WebSocket support for real-time restroom status updates

---

<div align="center">
  <p>Made with ❤️ for better public restroom accessibility</p>
  <p>Сделано с ❤️ для лучшей доступности общественных туалетов</p>
</div>