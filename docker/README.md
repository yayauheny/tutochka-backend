# Docker Startup Guide

Webhook - единственный поддерживаемый режим получения Telegram updates.

## Production
```bash
docker compose -f docker/docker-compose.prod.yml up -d --wait
```

По умолчанию production stack использует:
- `ghcr.io/yayauheny/tutochka-backend:prod`
- `ghcr.io/yayauheny/tutochka-bot:prod`

Для `backend` и `bot` включен `pull_policy: always`, поэтому повторный `docker compose up` подтянет свежий образ с тегом `prod` и пересоздаст контейнер, если образ обновился.

Rollback или точечный deploy на одинаковый immutable tag для обоих сервисов:
```bash
BACKEND_TAG=sha-0123456789ab BOT_TAG=sha-0123456789ab docker compose -f docker/docker-compose.prod.yml up -d --wait
```

Если нужно раскатить разные версии backend и bot:
```bash
BACKEND_TAG=sha-0123456789ab BOT_TAG=sha-fedcba987654 docker compose -f docker/docker-compose.prod.yml up -d --wait
```

## Full Local Stack
```bash
docker compose --env-file docker/env.local -f docker/docker-compose-local.yml up -d
docker compose --env-file docker/env.local -f docker/docker-compose-local.yml --profile ngrok up -d
```

Скопируй `docker/env.local.example` в `docker/env.local` и задай `BOT_WEBHOOK_SECRET_TOKEN` перед запуском webhook-режима.
В этом режиме backend запускается в Docker и всегда ходит в Postgres по имени хоста `postgres`.
Данные Postgres сохраняются в `docker/postgres-data`, поэтому они переживают `docker compose down` и удаляются только если убрать эту папку вручную.

Порядок запуска:
1. `postgres`
2. `backend`
3. `bot`
4. `ngrok` и `webhook-registrar` только если нужен внешний Telegram-доступ

Для webhook-режима обязательно задать `BOT_WEBHOOK_SECRET_TOKEN`, иначе регистрация webhook не выполнится.

## Hybrid Debug Stack
```bash
docker compose --env-file docker/env.local -f docker/docker-compose-debug.yml up -d
set -a; source docker/env.local; set +a
./gradlew :backend:run
./gradlew :bot:bootRun
```

В hybrid-режиме backend и bot работают на хосте, поэтому для них `DB_HOST` должен быть `localhost`.

## Проверка
```bash
curl http://localhost:8080/health/live
curl http://localhost:8081/actuator/health
curl http://localhost:4040/api/tunnels
```
