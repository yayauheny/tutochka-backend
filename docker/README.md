# Docker Startup Guide

Webhook - единственный поддерживаемый режим получения Telegram updates.

## Production
```bash
docker compose -f docker/docker-compose.prod.yml up -d
```

## Full Local Stack
```bash
docker compose --env-file docker/env.local -f docker/docker-compose-local.yml up -d
docker compose --env-file docker/env.local -f docker/docker-compose-local.yml --profile ngrok up -d
```

Порядок запуска:
1. `postgres`
2. `backend`
3. `bot`
4. `ngrok` и `webhook-registrar` только если нужен внешний Telegram-доступ

## Hybrid Debug Stack
```bash
docker compose --env-file docker/env.local -f docker/docker-compose-debug.yml up -d
./gradlew :backend:run
./gradlew :bot:bootRun
```

## Проверка
```bash
curl http://localhost:8080/health/live
curl http://localhost:8081/actuator/health
curl http://localhost:4040/api/tunnels
```
