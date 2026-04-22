# ТуТочка Backend

Монорепо с двумя активными модулями:
- `backend` - Ktor REST API, PostgreSQL/PostGIS, jOOQ, Liquibase.
- `bot` - Spring Boot Telegram bot, который ходит в backend по HTTP.

Источник правды для контрактов и маршрутов - код и `backend/src/main/resources/openapi.yaml`.

## Что есть
- `GET /health`, `GET /health/ready`, `GET /health/live`
- `GET /api/v1/countries`
- `GET /api/v1/cities`
- `GET /api/v1/restrooms`
- `GET /api/v1/restrooms/{id}`
- `GET /api/v1/restrooms/city/{cityId}`
- `GET /api/v1/restrooms/nearest`
- `POST /api/v1/import`
- `POST /api/v1/import/batch`

## Быстрый старт
- Backend: `./gradlew :backend:run`
- Bot: `./gradlew :bot:bootRun`
- Docker: [docker/README.md](docker/README.md)

## Конфигурация
- Backend config: `backend/src/main/resources/application.yaml`
- Bot config: `bot/src/main/resources/application.yml`
- API contract: `backend/src/main/resources/openapi.yaml`

## Документация
- [docs/architecture/TECHNICAL_OVERVIEW.md](docs/architecture/TECHNICAL_OVERVIEW.md)
- [docs/DOCS_INDEX.md](docs/DOCS_INDEX.md)
- [bot/README.md](bot/README.md)
- [docker/README.md](docker/README.md)
