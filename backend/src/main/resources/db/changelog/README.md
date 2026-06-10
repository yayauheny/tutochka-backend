# DB Changelog Layout

## Active Files

- `db.changelog-master.yml` points to `legacy/` and preserves the current runtime behavior.
- `db.changelog-clean.yml` is the new clean bootstrap line for a fresh database branch.

## Root Files For The New Clean Bootstrap

- `001_extensions.sql` - database extensions
- `010_tables.sql` - final table definitions
- `020_indexes.sql` - all indexes in one place
- `030_constraints.sql` - non-table constraints and FK refinements
- `040_seed_core.sql` - non-reference startup data
- `050_checks.sql` - validation queries for a fresh bootstrap

## Directories

- `legacy/` - fallback copy of the current historical Liquibase line
- `reference_data/` - reference data sources and UUID notes
- `reference_load/` - Liquibase loaders for reference data

## Naming For Future Change Files

- Clean bootstrap base files use ordered numeric prefixes: `001`, `010`, `020`, ...
- Future changes after the clean bootstrap should use `YYYYMMDD_HHMM_description.sql`.
- If several files are created close together, prefer full minute or second precision over artificial suffixes.
