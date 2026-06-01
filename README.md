# ТуТочка Backend

Монорепо с активным production runtime в модуле `backend`:
- `backend` - Spring Boot REST API, PostgreSQL/PostGIS, Liquibase, Telegram webhook runtime и import runtime.

Источник правды для контрактов и маршрутов - код и `backend/src/main/resources/openapi.yaml`.

## Что есть
- `GET /health`, `GET /health/ready`, `GET /health/live`
- `GET /api/v1/countries`
- `GET /api/v1/cities`
- `GET /api/v1/restrooms`
- `GET /api/v1/restrooms/{id}`
- `GET /api/v1/restrooms/city/{cityId}`
- `GET /api/v1/restrooms/nearest`
- `POST /api/v1/import/batch`
- `POST /api/v1/analytics/events`
- `GET /api/v1/public/analytics`

## Быстрый старт
- Backend: `./gradlew :backend:run`
- Docker: [docker/README.md](docker/README.md)

## Конфигурация
- Backend config: `backend/src/main/resources/application.yml`
- API contract: `backend/src/main/resources/openapi.yaml`
- Backend secret for analytics encryption: set `ENCRYPTION_KEYSET_JSON`
- Telegram runtime envs: `TELEGRAM_BOT_TOKEN`, `BOT_WEBHOOK_SECRET_TOKEN`, optional `BOT_WEBHOOK_PUBLIC_URL`

## Документация
- [docs/architecture/TECHNICAL_OVERVIEW.md](docs/architecture/TECHNICAL_OVERVIEW.md)
- [docs/DOCS_INDEX.md](docs/DOCS_INDEX.md)
- [docker/README.md](docker/README.md)
