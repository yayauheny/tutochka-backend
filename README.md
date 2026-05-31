# ТуТочка Backend

Монорепо с активным модулем `backend`:
- `backend` - Spring Boot REST API, PostgreSQL/PostGIS, Liquibase и Telegram bot runtime.

Источник правды для контрактов и маршрутов - код и `backend/src/main/resources/openapi.yaml`.

## Что есть
- `GET /health`, `GET /health/ready`, `GET /health/live`
- `GET /api/v1/countries`
- `GET /api/v1/cities`
- `GET /api/v1/restrooms`
- `GET /api/v1/restrooms/{id}`
- `GET /api/v1/restrooms/city/{cityId}`
- `GET /api/v1/restrooms/nearest`
- `POST /api/v1/analytics/events`
- `GET /api/v1/public/analytics`
- `POST /api/v1/import/batch` is the only import endpoint and accepts either a raw JSON array or `{ "items": [...] }`

## Быстрый старт
- Backend: `./gradlew :backend:run`
- Docker: [docker/README.md](docker/README.md)

## Конфигурация
- Backend config: `backend/src/main/resources/application.yml`
- API contract: `backend/src/main/resources/openapi.yaml`
- Backend secret for analytics encryption: set `ENCRYPTION_KEYSET_JSON`

## Документация
- [docs/architecture/TECHNICAL_OVERVIEW.md](docs/architecture/TECHNICAL_OVERVIEW.md)
- [docs/DOCS_INDEX.md](docs/DOCS_INDEX.md)
- [docker/README.md](docker/README.md)
