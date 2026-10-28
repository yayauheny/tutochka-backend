# DB Changelog Layout

## Active Files

- `db.changelog.yml` is the canonical bootstrap line for a fresh database branch.

## Root Files For The New Clean Bootstrap

- `db.changelog.yml` - bootstrap entrypoint
- `001_extensions.sql` - database extensions
- `010_core_tables.sql` - core tables
- `011_user_tables.sql` - user/privacy tables and inline indexes
- `012_analytics_tables.sql` - analytics tables

## Directories

- `reference_data/` - reference data sources and UUID notes
- `reference_load/` - Liquibase loaders for reference data

## Naming For Future Change Files

- Clean bootstrap base files use ordered numeric prefixes: `001`, `010`, `011`, `012`.
- Future changes after the clean bootstrap should use `YYYYMMDD_HHMM_description.sql`.
- If several files are created close together, prefer full minute or second precision over artificial suffixes.
