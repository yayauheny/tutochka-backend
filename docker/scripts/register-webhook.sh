#!/bin/sh
set -e

NGROK_HOST="${NGROK_HOST:-ngrok}"
NGROK_PORT="${NGROK_PORT:-4040}"
TELEGRAM_BOT_TOKEN="${TELEGRAM_BOT_TOKEN:?TELEGRAM_BOT_TOKEN required}"
BOT_WEBHOOK_PATH="${BOT_WEBHOOK_PATH:-/telegram/webhook}"

echo "Waiting for ngrok API at http://${NGROK_HOST}:${NGROK_PORT}/api/tunnels..."
until curl -sf "http://${NGROK_HOST}:${NGROK_PORT}/api/tunnels" > /tmp/tunnels.json 2>/dev/null; do
  sleep 2
done

NGROK_URL=$(grep -o '"public_url":"https://[^"]*"' /tmp/tunnels.json | head -1 | cut -d'"' -f4)
if [ -z "$NGROK_URL" ]; then
  echo "ERROR: No HTTPS tunnel found in ngrok response"
  cat /tmp/tunnels.json
  exit 1
fi

WEBHOOK_URL="${NGROK_URL}${BOT_WEBHOOK_PATH}"
echo "Setting Telegram webhook to: $WEBHOOK_URL"

RESPONSE=$(curl -sf "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook?url=${WEBHOOK_URL}")
echo "Telegram API response: $RESPONSE"
