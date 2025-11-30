# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-11-30

### Added
- REST API for managing countries, cities, and restrooms
- Location-based search functionality using PostGIS
- Comprehensive validation system for all endpoints
- Error handling with structured error responses
- Pagination support for all list endpoints
- Health check endpoint (`/health`)
- OpenAPI 3.0 specification and Swagger UI documentation
- Docker support with Dockerfile and docker-compose.yml
- Full test coverage with unit and integration tests
- Database migrations using Liquibase
- jOOQ for type-safe SQL queries
- Koin dependency injection framework
- Comprehensive logging with Logback

### Technical Details
- Kotlin 2.1.10
- Ktor 3.2.3
- PostgreSQL with PostGIS extension
- JDK 21 (LTS)
- Gradle with Kotlin DSL

### API Endpoints
- `GET /api/v1/countries` - List all countries with pagination
- `GET /api/v1/countries/{id}` - Get country by ID
- `GET /api/v1/countries/code/{code}` - Get country by code
- `GET /api/v1/cities` - List all cities with pagination
- `GET /api/v1/cities/{id}` - Get city by ID
- `GET /api/v1/cities/country/{countryId}` - Get cities by country
- `GET /api/v1/cities/search` - Search cities by name
- `GET /api/v1/restrooms` - List all restrooms with pagination
- `GET /api/v1/restrooms/{id}` - Get restroom by ID
- `GET /api/v1/restrooms/city/{cityId}` - Get restrooms by city
- `GET /api/v1/restrooms/nearest` - Find nearest restrooms by coordinates
- `POST /api/v1/countries` - Create new country
- `POST /api/v1/cities` - Create new city
- `POST /api/v1/restrooms` - Create new restroom

### Documentation
- README.md with installation and usage instructions (EN/RU)
- OpenAPI specification at `/swagger-ui.html`
- Architecture documentation

[0.1.0]: https://github.com/your-org/tutochka-backend/releases/tag/v0.1.0
