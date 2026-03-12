# Инструкция по деплою TuTochka Backend и Telegram Bot

## Подготовка к деплою

### 1. Требования

- SSH доступ к серверу (GitHub Actions, или прямой SSH)
- Docker и Docker Compose установлены на сервере
- Домен настроен и указывает на IP сервера (для webhook)
- SSL сертификат (Let's Encrypt через certbot или другой провайдер)

### 2. Переменные окружения

Создайте файл `.env` в директории `docker/` на основе `docker/env.example`:

```bash
cd docker/
cp .env .env
nano .env  # или используйте ваш любимый редактор
```

**Обязательные переменные для production:**

```bash
# Database
POSTGRES_USER=your_secure_db_user
POSTGRES_PASSWORD=your_secure_db_password
POSTGRES_DB=tutochka_db

DB_HOST=postgres
DB_PORT=5432
DB_NAME=tutochka_db
DB_USER=your_secure_db_user
DB_PASSWORD=your_secure_db_password

# Telegram Bot
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_BOT_TOKEN=your_bot_token_from_botfather
BOT_WEBHOOK_PATH=/telegram/webhook
BOT_ADMIN_IDS=123456789,987654321  # Telegram user IDs через запятую

# Backend
BACKEND_BASE_URL=http://backend:8080/api/v1  # Для внутренней сети Docker
```

**Важно:** Никогда не коммитьте `.env` файл в git! Он уже добавлен в `.gitignore`.

## Деплой через SSH (GitHub Actions или прямой SSH)

### Шаг 1: Подготовка сервера

```bash
# Подключитесь к серверу
ssh user@your-server-ip

# Установите Docker и Docker Compose (если не установлены)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Установите Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Проверьте установку
docker --version
docker-compose --version
```

### Шаг 2: Клонирование репозитория

```bash
# Создайте директорию для проекта
mkdir -p ~/tutochka
cd ~/tutochka

# Клонируйте репозиторий (или используйте git pull если уже клонирован)
git clone https://github.com/your-username/tutochka-backend.git .
# или
git pull origin main  # если репозиторий уже существует
```

### Шаг 3: Настройка переменных окружения

```bash
cd docker/
cp .env .env
nano .env  # Заполните все необходимые переменные (см. выше)
```

### Шаг 4: Настройка Nginx (для HTTPS и проксирования)

```bash
# Установите Nginx
sudo apt update
sudo apt install nginx certbot python3-certbot-nginx -y

# Создайте конфигурацию для вашего домена
sudo nano /etc/nginx/sites-available/tutochka
```

**Конфигурация Nginx (`/etc/nginx/sites-available/tutochka`):**

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Редирект на HTTPS (будет настроен certbot)
    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL сертификаты (будут настроены certbot)
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    # SSL настройки
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Проксирование на backend (API)
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Таймауты для долгих запросов
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint
    location /health {
        proxy_pass http://localhost:8080/health;
        proxy_set_header Host $host;
    }

    # Проксирование webhook для Telegram бота
    location /telegram/webhook {
        proxy_pass http://localhost:8081/telegram/webhook;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Важно: Telegram требует HTTPS для webhook
        proxy_ssl_verify off;
    }

    # Логирование
    access_log /var/log/nginx/tutochka_access.log;
    error_log /var/log/nginx/tutochka_error.log;
}
```

```bash
# Активируйте конфигурацию
sudo ln -s /etc/nginx/sites-available/tutochka /etc/nginx/sites-enabled/
sudo nginx -t  # Проверьте конфигурацию
sudo systemctl reload nginx
```

### Шаг 5: Получение SSL сертификата

```bash
# Получите SSL сертификат от Let's Encrypt
sudo certbot --nginx -d your-domain.com

# Certbot автоматически обновит конфигурацию Nginx
# Проверьте что сертификат обновляется автоматически
sudo certbot renew --dry-run
```

### Шаг 6: Сборка и запуск контейнеров

```bash
cd ~/tutochka/docker/

# Соберите образы (первый раз может занять время)
docker-compose build --no-cache

# Запустите контейнеры
docker-compose up -d

# Проверьте статус
docker-compose ps
docker-compose logs -f  # Для просмотра логов
```

### Шаг 7: Проверка работоспособности

```bash
# Проверьте health endpoints
curl http://localhost:8080/health  # Backend
curl http://localhost:8081/actuator/health  # Bot

# Проверьте через домен (должен работать HTTPS)
curl https://your-domain.com/health
curl https://your-domain.com/api/v1/restrooms/nearest?lat=53.9&lon=27.5&radius=1000
```

### Шаг 8: Настройка webhook для Telegram бота

```bash
# Установите webhook через Telegram API
curl -X POST "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://your-domain.com/telegram/webhook"}'

# Проверьте что webhook установлен
curl "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getWebhookInfo"
```

**Важно:** Замените `<YOUR_BOT_TOKEN>` на реальный токен бота.

### Шаг 9: Настройка автоматического обновления (опционально)

Создайте скрипт для автоматического обновления при пуше в main ветку:

```bash
nano ~/tutochka/deploy.sh
```

**Содержимое `deploy.sh`:**

```bash
#!/bin/bash
set -e

cd ~/tutochka

echo "Pulling latest changes..."
git pull origin main

echo "Building Docker images..."
cd docker/
docker-compose build

echo "Restarting services..."
docker-compose up -d

echo "Waiting for services to be healthy..."
sleep 10

echo "Checking health..."
curl -f http://localhost:8080/health || exit 1
curl -f http://localhost:8081/actuator/health || exit 1

echo "Deployment completed successfully!"
```

```bash
chmod +x ~/tutochka/deploy.sh
```

## Деплой через GitHub Actions

Создайте файл `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches:
      - main

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SSH_HOST }}
          username: ${{ secrets.SSH_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd ~/tutochka
            git pull origin main
            cd docker/
            docker-compose build
            docker-compose up -d
            sleep 10
            curl -f http://localhost:8080/health || exit 1
            curl -f http://localhost:8081/actuator/health || exit 1
```

**Настройте secrets в GitHub:**
- `SSH_HOST` - IP адрес сервера
- `SSH_USER` - имя пользователя для SSH
- `SSH_PRIVATE_KEY` - приватный SSH ключ

## Мониторинг и логи

### Просмотр логов

```bash
# Все сервисы
docker-compose logs -f

# Конкретный сервис
docker-compose logs -f backend
docker-compose logs -f bot
docker-compose logs -f postgres

# Последние 100 строк
docker-compose logs --tail=100 backend
```

### Мониторинг ресурсов

```bash
# Использование ресурсов контейнерами
docker stats

# Проверка дискового пространства
docker system df
```

### Ротация логов

Логи автоматически ротируются через Docker (max-size: 10m, max-file: 3).

## Обновление приложения

```bash
cd ~/tutochka

# Получите последние изменения
git pull origin main

# Пересоберите и перезапустите
cd docker/
docker-compose build
docker-compose up -d

# Проверьте что все работает
docker-compose ps
curl http://localhost:8080/health
```

## Откат к предыдущей версии

```bash
cd ~/tutochka

# Переключитесь на предыдущий коммит
git checkout <previous-commit-hash>

# Пересоберите и перезапустите
cd docker/
docker-compose build
docker-compose up -d
```

## Резервное копирование базы данных

```bash
# Создайте бэкап
docker-compose exec postgres pg_dump -U admin tutochka_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Восстановление из бэкапа
docker-compose exec -T postgres psql -U admin tutochka_db < backup_20240101_120000.sql
```

## Устранение неполадок

### Контейнеры не стартуют

```bash
# Проверьте логи
docker-compose logs

# Проверьте статус
docker-compose ps

# Пересоздайте контейнеры
docker-compose down
docker-compose up -d
```

### База данных не подключается

```bash
# Проверьте что postgres контейнер запущен
docker-compose ps postgres

# Проверьте логи postgres
docker-compose logs postgres

# Проверьте переменные окружения
docker-compose exec backend env | grep DB_
```

### Webhook не работает

1. Проверьте что webhook установлен:
   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/getWebhookInfo"
   ```

2. Проверьте что Nginx проксирует запросы:
   ```bash
   sudo tail -f /var/log/nginx/tutochka_access.log
   ```

3. Проверьте логи бота:
   ```bash
   docker-compose logs bot | grep webhook
   ```

### Проблемы с SSL

```bash
# Проверьте сертификат
sudo certbot certificates

# Обновите сертификат вручную
sudo certbot renew

# Проверьте конфигурацию Nginx
sudo nginx -t
```

## Безопасность

1. **Никогда не коммитьте секреты** в git
2. **Используйте сильные пароли** для базы данных
3. **Ограничьте доступ** к портам базы данных (не пробрасывайте наружу)
4. **Регулярно обновляйте** Docker образы и зависимости
5. **Мониторьте логи** на подозрительную активность
6. **Используйте firewall** (ufw, iptables) для ограничения доступа

## Контакты и поддержка

При возникновении проблем проверьте:
- Логи контейнеров: `docker-compose logs`
- Логи Nginx: `/var/log/nginx/tutochka_*.log`
- Статус сервисов: `docker-compose ps`
- Health endpoints: `curl http://localhost:8080/health`
