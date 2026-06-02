# Testing

## Layers

- `backend/src/test/kotlin/**/unit/**`: plain JVM tests and Spring slice tests that do not boot the full application.
- `backend/src/test/kotlin/**/unit/controller/**`: `@WebMvcTest` controller slice tests.
- `backend/src/test/kotlin/**/unit/repository/**`: `@DataJpaTest` repository slice tests backed by Testcontainers Postgres.
- `backend/src/test/kotlin/**/integrationTest/**`: `@SpringBootTest` integration tests.
- `common/src/test/kotlin/**`: `common` module tests.

## Commands

- `./gradlew check`: default verification for the whole repository. Runs module tests, backend integration tests, coverage verification, and taxonomy validation.
- `./gradlew :backend:test`: backend unit and slice tests.
- `./gradlew :backend:integrationTest`: backend integration tests only.

## Testcontainers

- Container reuse is disabled by default so CI and local clean runs are isolated.
- Local reuse can be enabled explicitly with `./gradlew :backend:integrationTest -Dtestcontainers.reuse=true`.

## Coverage

- Coverage verification is part of `check` for both `backend` and `common`.
- `backend` coverage includes unit, slice, and integration executions through the JaCoCo report task.
