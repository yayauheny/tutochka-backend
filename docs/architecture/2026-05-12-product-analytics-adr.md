# ADR-0001: Product analytics for bot and backend

Date: 2026-05-12

## Status

Accepted

## Context

We added product analytics to the `tutochka-backend` stack to observe user behavior without making analytics part of the core restroom search flow.

Before this change there was no dedicated analytics model, no analytics endpoint, and no persistence path for bot-side product events. We needed to support:

- Telegram bot events such as restroom searches and restroom detail opens
- backend-side search analytics
- anonymous analytics events for direct API callers
- reliable user-facing behavior even when analytics storage fails

## Decision

We introduced a dedicated analytics slice in the backend and wired the bot to send product events to it.

### Data model

- Added `users`, `user_analytics`, and `analytics_events` tables.
- Kept `users.tg_user_id` as the Telegram user identifier and stored it encrypted at rest.
- Kept `users.tg_chat_id` encrypted at rest.
- Kept `users.username` in plain text.
- Removed `city` from analytics storage and used coordinates as the stable location dimensions.
- Kept `analytics_events.user_id` nullable for anonymous events.
- Removed the unique constraint on `users.tg_chat_id` so users in the same chat do not collide.

### Contract shape

- The shared analytics contract uses numeric Telegram user ids (`Long`) at the API boundary.
- Telegram chat id remains a string header value because it is stored encrypted and is not used as a numeric key.
- The backend analytics endpoint accepts:
  - identified Telegram events when Telegram headers are present
  - anonymous API events when `source=api`

### Backend behavior

- Added `POST /api/v1/analytics/events`.
- Added analytics tracking for `/api/v1/restrooms/nearest`.
- Added analytics tracking for restroom detail opens from the bot.
- Business workflows remain non-blocking:
  - restroom search still returns `200` even if analytics tracking fails
  - bot callbacks still respond even if analytics tracking fails
- Analytics request validation is strict:
  - malformed JSON and invalid payloads return `400`
  - internal analytics storage failures are logged and do not break the user flow

### Bot behavior

- The bot sends analytics headers with Telegram user id, chat id, and username.
- Analytics POST requests do not use retry logic, to avoid duplicate write-side events.
- Read-side requests such as nearest-search and detail fetches keep retry behavior.

## Consequences

### Positive

- Analytics is now observable without changing the core restroom UX.
- Identified Telegram events can be aggregated per user.
- Anonymous API callers can still create analytics events.
- Database collisions from shared Telegram chats are avoided.

### Tradeoffs

- Analytics is best-effort for user flows, so metrics can be lost if the analytics subsystem is down.
- Telegram user ids are encrypted at rest, so debugging requires the encryption keyset in trusted environments.
- The analytics schema is intentionally smaller: we do not store `city` because coordinates are enough for the current use cases.

## Verification

The following focused tests cover the new behavior:

- `AnalyticsApiTest`
- `RestroomMetricsIntegrationTest`
- `WebBackendClientHttpTest`

They verify:

- encrypted `tg_user_id` and `tg_chat_id`
- plain `username`
- anonymous API analytics
- same-chat multi-user writes
- invalid analytics payloads returning `400`
- non-blocking restroom search when analytics fails
- no-retry bot analytics POSTs
