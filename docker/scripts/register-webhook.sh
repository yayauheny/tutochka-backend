#!/bin/sh
set -e

NGROK_HOST="${NGROK_HOST:-ngrok}"
NGROK_PORT="${NGROK_PORT:-4040}"
TELEGRAM_BOT_TOKEN="${TELEGRAM_BOT_TOKEN:?TELEGRAM_BOT_TOKEN required}"
BOT_WEBHOOK_PATH="${BOT_WEBHOOK_PATH:-/telegram/webhook}"
BOT_WEBHOOK_SECRET_TOKEN="${BOT_WEBHOOK_SECRET_TOKEN:?BOT_WEBHOOK_SECRET_TOKEN required}"

echo "Waiting for ngrok API and tunnel establishment at http://${NGROK_HOST}:${NGROK_PORT}/api/tunnels..."
NGROK_URL=""
while [ -z "$NGROK_URL" ]; do
  curl -sf "http://${NGROK_HOST}:${NGROK_PORT}/api/tunnels" > /tmp/tunnels.json 2>/dev/null || true
  if [ -f /tmp/tunnels.json ]; then
    NGROK_URL=$(grep -o '"public_url":"https://[^"]*"' /tmp/tunnels.json | head -1 | cut -d'"' -f4)
  fi
  if [ -z "$NGROK_URL" ]; then
    sleep 2
  fi
done

WEBHOOK_URL="${NGROK_URL}${BOT_WEBHOOK_PATH}"
echo "Setting Telegram webhook to: $WEBHOOK_URL"

RESPONSE=$(curl -sfG "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook" \
  --data-urlencode "url=${WEBHOOK_URL}" \
  --data-urlencode "secret_token=${BOT_WEBHOOK_SECRET_TOKEN}")
echo "Telegram API response: $RESPONSE"
