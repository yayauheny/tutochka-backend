# TODO: Переезд на OpenAPI контракт + генерация клиентов

## Контекст

Сейчас bot (Java/Spring) и backend (Kotlin) используют разные DTO (дубли),

потому что попытка шарить Kotlin DTO через shared модуль привела к проблемам

с message converters (Jackson перехватывает JSON раньше KotlinSerializationJsonHttpMessageConverter),

а также к проблемам сериализации UUID/Instant.



Цель: перейти на контрактный подход: OpenAPI -> генерация моделей/клиентов.



## Решение (целевое)

1) Backend публикует OpenAPI спецификацию:

   - /v3/api-docs (json)

   - /swagger-ui (опционально)

2) Bot генерирует клиент и DTO из OpenAPI:

   - либо Java models + Jackson (рекомендуется для Spring bot)

   - либо Kotlin client (если бот станет на Kotlin)

3) Версионирование контракта: v1, v2 (semver), чтобы обновлять бота безопасно.



## План работ

- [ ] Выбрать генератор: openapi-generator (maven/gradle plugin)

- [ ] Выбрать формат: OpenAPI 3.0/3.1

- [ ] На backend добавить генерацию/экспорт спеки:

      - вариант A: аннотации + springdoc (если backend на Spring)

      - вариант B: Ktor OpenAPI plugin (если backend на Ktor)

      - вариант C: хранить openapi.yaml вручную (самый контролируемый)

- [ ] Добавить в CI проверку совместимости:

      - сравнение openapi.yaml между ветками

      - запрет breaking changes без bump версии

- [ ] Вынести openapi.yaml в отдельный модуль (вместо shared DTO):

      module :contract (или :openapi)

      - хранит openapi.yaml

      - генерит модели/клиент для bot (и при желании для других клиентов)

- [ ] Подключить генерацию в сборку bot:

      - generate sources -> compileJava

- [ ] Удалить дубли DTO после миграции, когда клиент полностью на OpenAPI



## Технические заметки

- UUID/Instant: в OpenAPI описывать как

  - UUID: type: string, format: uuid

  - Instant: type: string, format: date-time

- Snake_case vs camelCase: зафиксировать в контракте.

- Если используются oneOf/anyOf — убедиться, что генерация корректно поддерживает.

