# Work Time Unification Summary

## Goal

Unify restroom working-time storage and display so the backend can:

1. keep provider data in a single canonical shape;
2. show current open/closed status in the bot;
3. preserve the source verification date when it exists;
4. support fast database filtering for:
   - open now;
   - closed now;
   - opens soon.

## Current state

### Import layer

The import subsystem already separates provider parsing from the generic import pipeline:

- `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/*`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/*`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/osm/*`
- `backend/src/main/kotlin/yayauheny/by/importing/mapper/RestroomImportMapper.kt`
- `backend/src/main/kotlin/yayauheny/by/model/import/NormalizedRestroomCandidate.kt`

But the schedule payload itself is still provider-shaped:

- 2GIS keeps a day-keyed structure with `working_hours` arrays.
- Yandex keeps a day-keyed structure with `from`, `to`, and optional `intervals`.
- OSM keeps `opening_hours` plus optional `check_date`.

That means the backend stores different JSON shapes in the same `workTime` column.

### Runtime schedule handling

The active runtime resolves schedules through:

- `backend/src/main/kotlin/yayauheny/by/bot/service/RestroomAvailabilityResolver.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/ScheduleMappingService.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/OsmScheduleAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/TwoGisScheduleAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/YandexScheduleAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/util/ScheduleUtils.kt`

The canonical internal model is:

- `backend/src/main/kotlin/yayauheny/by/model/schedule/Schedule.kt`
- `backend/src/main/kotlin/yayauheny/by/model/schedule/DaySchedule.kt`
- `backend/src/main/kotlin/yayauheny/by/model/schedule/WorkingInterval.kt`
- `backend/src/main/kotlin/yayauheny/by/model/schedule/Weekday.kt`

This model is enough for simple weekday intervals and 24/7, but not for full OSM `opening_hours` semantics.

### Bot formatting

The bot detail card is assembled in:

- `backend/src/main/kotlin/yayauheny/by/bot/service/FormatterService.kt`
- `backend/src/main/kotlin/yayauheny/by/bot/messages/Messages.kt`
- `backend/src/main/kotlin/yayauheny/by/bot/util/WorkTimeFormatter.kt`

The detail text uses `ResolvedWorkTime.workTimeText`, which comes from `RestroomAvailabilityResolver.resolve(...)`.

## What is not unified now

### 1. Multiple work-time shapes

There is no single canonical persistence schema for schedules.

Current shapes in the codebase:

- `{"Mon": {"working_hours": [...]}}`
- `{"mon": {"from": "...", "to": "...", "intervals": [...]}}`
- `{"opening_hours": "Mo-Fr 09:00-18:00", "check_date": "2025-01-01"}`
- test/default helper shape like `{"monday": "09:00-18:00"}`

This makes DB-level filtering inconsistent and makes the UI depend on provider-specific heuristics.

### 2. OSM format is only partially supported

The current OSM adapter accepts only a small subset:

- `24/7`
- a simple daily range like `07:00-23:00`

It does not parse real OSM expressions such as:

- weekdays with exceptions;
- multiple intervals per day;
- holiday rules;
- seasonal closures;
- complex semicolon-separated rules.

So the project is not yet using the full OSM `opening_hours` model.

### 3. Date metadata is preserved in OSM input but lost in display

`OsmNormalizationProfile` stores:

- `opening_hours`
- `check_date` when `check_date:opening_hours` exists

But the bot path never formats `check_date`.

Root cause:

- `RestroomAvailabilityResolver.resolve(...)` maps raw work time to `Schedule`.
- `FormatterService.toiletDetail(...)` prints only `availability.workTimeText`.
- `WorkTimeFormatter.formatSchedule(...)` prints normalized hours only.
- `WorkTimeFormatter.formatWorkTime(...)` returns `opening_hours` early and ignores `check_date`.

So the date exists in raw OSM payloads but is not shown in the detail view.

### 4. No database field for open now / next opening

The code currently checks availability in application memory.

There is no repository-level or SQL-level filter for:

- `open_now`
- `closed_now`
- `opens_soon`
- `next_change_at`

That means the statement “filter quickly in DB” is not satisfied today.

### 5. Provider heuristics are still ad hoc

`RestroomAvailabilityResolver.effectiveProvider(...)` uses a shape heuristic for `USER` and `MANUAL` rows.

That is useful as a fallback, but it is not a real canonicalization step. It only guesses the parser based on field names.

## Why the bot does not show the working-date

The missing date is not a Telegram issue and not a rendering bug in the callback handler.

The concrete path is:

1. The user opens a toilet detail card.
2. `ToiletDetailCallback` calls `FormatterService.toiletDetail(...)`.
3. `FormatterService` asks `RestroomAvailabilityResolver` for `ResolvedWorkTime`.
4. The resolver either:
   - maps the raw work time to `Schedule`, or
   - falls back to the raw string.
5. The final template prints `workTimeText` only.
6. No step appends `check_date`.

So the date is dropped by design of the current formatter path.

## Recommended canonical solution

### Canonical storage shape

Use one normalized structure for all providers, conceptually aligned with OSM:

```json
{
  "opening_hours": "Mo-Fr 09:00-18:00; Sa 10:00-14:00",
  "check_date": "2026-06-25",
  "timezone": "Europe/Minsk",
  "source": "osm|2gis|yandex|manual",
  "format": "osm_opening_hours",
  "notes": "optional human-readable fallback"
}
```

If a provider cannot be expressed as a valid OSM `opening_hours` string, preserve:

- the raw provider payload;
- a human-readable fallback string;
- a normalized status summary.

### Derived fields for fast filtering

To make DB filtering fast, add derived fields instead of relying on JSON parsing at query time:

- `work_time_open_now` boolean
- `work_time_next_change_at` timestamp
- `work_time_is_24x7` boolean
- `work_time_checked_at` date
- `work_time_format` enum or small text field

This gives two layers:

1. raw canonical schedule data for display and auditing;
2. indexed derived fields for search/filter performance.

### Application flow

Recommended flow:

1. provider parser extracts raw schedule;
2. normalizer converts it to canonical `opening_hours` payload;
3. scheduler/parser computes current state and next transition;
4. persistence stores both raw canonical payload and derived fields;
5. bot reads derived state for labels and the canonical payload for display.

## Files that define the current behavior

- `backend/src/main/kotlin/yayauheny/by/importing/provider/osm/OsmNormalizationProfile.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/yandex/YandexNormalizationProfile.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/provider/twogis/TwoGisNormalizationProfile.kt`
- `backend/src/main/kotlin/yayauheny/by/importing/mapper/RestroomImportMapper.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/OsmScheduleAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/TwoGisScheduleAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/service/import/schedule/YandexScheduleAdapter.kt`
- `backend/src/main/kotlin/yayauheny/by/bot/service/RestroomAvailabilityResolver.kt`
- `backend/src/main/kotlin/yayauheny/by/bot/service/FormatterService.kt`
- `backend/src/main/kotlin/yayauheny/by/bot/util/WorkTimeFormatter.kt`
- `backend/src/main/kotlin/yayauheny/by/util/ScheduleUtils.kt`

## Formatting / consistency notes

The import package is generally split cleanly by provider, but schedule-related naming is still mixed:

- `rawSchedule`
- `workTime`
- `opening_hours`
- `working_hours`
- `workingHours`
- `intervals`

This makes the code harder to scan and increases the chance of losing fields during conversion.

The main consistency rule should be:

1. keep provider input names only at the edge;
2. convert them to one internal schedule model immediately;
3. do not reintroduce provider-specific shape in domain or bot formatting code.

## Bottom line

The current implementation can show a basic open/closed status, but it does not yet provide a truly unified work-time model.

The specific reason the bot does not show the work date is that `check_date` is stored in raw OSM data but never propagated into `ResolvedWorkTime` or `FormatterService` output.

The right next step is to introduce one canonical schedule payload, keep the raw source string, and persist derived fields for fast filtering.
