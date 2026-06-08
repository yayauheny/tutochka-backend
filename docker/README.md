# Docker Startup Guide

Webhook - единственный поддерживаемый режим получения Telegram updates.

## Production
```bash
docker compose -f docker/docker-compose.yml up -d --wait
```

По умолчанию production stack использует:
- `ghcr.io/yayauheny/tutochka-backend:prod`

Для `backend` включен `pull_policy: always`, поэтому повторный `docker compose up` подтянет свежий образ с тегом `prod` и пересоздаст контейнер, если образ обновился.

Rollback или точечный deploy на immutable tag:
```bash
BACKEND_TAG=sha-0123456789ab docker compose -f docker/docker-compose.yml up -d --wait
```

## Full Local Stack
```bash
docker compose --env-file docker/env.local -f docker/docker-compose-local.yml up -d --wait
docker compose --env-file docker/env.local -f docker/docker-compose-local.yml --profile ngrok up -d --wait
```

Скопируй `docker/env.local.example` в `docker/env.local` и задай `ENCRYPTION_KEYSET_JSON`, `TELEGRAM_BOT_TOKEN`, `BOT_WEBHOOK_SECRET_TOKEN`.
Если нужен внешний Telegram-доступ, также задай `BOT_WEBHOOK_PUBLIC_URL` и `NGROK_AUTHTOKEN`.
В этом режиме backend запускается в Docker и всегда ходит в Postgres по имени хоста `postgres`.
Данные Postgres сохраняются в `docker/postgres-data`, поэтому они переживают `docker compose down` и удаляются только если убрать эту папку вручную.

Порядок запуска:
1. `postgres`
2. `backend`
3. `ngrok` только если нужен внешний Telegram-доступ

Для webhook-режима обязательно задать `BOT_WEBHOOK_SECRET_TOKEN`, иначе регистрация webhook не выполнится.

## Проверка
```bash
curl http://localhost:8080/health/live
curl http://localhost:4040/api/tunnels
```
