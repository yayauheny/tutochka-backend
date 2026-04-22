# Общие замечания по техдолгу

Этот файл покрывает только cross-cutting проблемы. Внутренние проблемы bot и backend вынесены в отдельные документы.

## Critical

- [ ] **Нет единого источника правды для контрактов, DTO и enum-ов**
   Файлы: `README.md:7-8`, `backend/src/main/resources/openapi.yaml`, `bot/src/main/java/by/yayauheny/tutochkatgbot/integration/BackendClient.java:9-12`, `bot/src/main/java/by/yayauheny/tutochkatgbot/dto/backend/`, `backend/src/main/kotlin/yayauheny/by/model/`.

   Проблема: README утверждает, что код и OpenAPI являются источником правды, но на практике bot держит ручные копии backend DTO, а backend - собственное дерево моделей. Комментарий в `BackendClient` даже говорит про shared DTOs, хотя общего модуля нет.

   Почему это критично: любая правка API требует синхронно менять backend, bot и документацию. Это гарантированный дрейф и основной источник поломок на границе модулей.

   Что делать: вынести контракт в отдельный `contract` или `api` module, либо генерировать bot-клиент и модели из OpenAPI. После этого убрать ручные копии DTO и enum-ов.

## Medium

- [ ] **Документация и runtime-config живут отдельно от кода**
   Файлы: `README.md`, `bot/README.md`, `docs/architecture/*`, `bot/src/main/resources/application.yml:5-23`, `backend/src/main/resources/openapi.yaml`.

   Проблема: один и тот же runtime-поток описан сразу в нескольких местах. В них одновременно живут URL, webhook path, endpoint list, default timeout-ы и контрактные ответы.

   Почему это плохо: часть документов уже устаревает отдельно от кода. Чем больше точек описания, тем больше ложных инструкций для разработчика и тем выше шанс сломать интеграцию при следующем изменении.

   Что делать: оставить один источник для контрактов и один источник для runtime defaults. Остальное либо генерировать, либо хотя бы валидировать в CI.

- [ ] **Контракт между bot и backend не версионирован**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/dto/backend/`, `bot/src/main/java/by/yayauheny/tutochkatgbot/integration/BackendClient.java`, `backend/src/main/kotlin/yayauheny/by/model/`.

   Проблема: bot работает на собственных копиях моделей и не имеет отдельного слоя совместимости, через который можно безопасно менять backend API.

   Почему это плохо: изменения в backend не фиксируются на уровне версии контракта, поэтому несовместимость обычно обнаруживается уже в рантайме или в ручном тестировании.

   Что делать: завести версионированный контрактный модуль, OpenAPI client generation или хотя бы отдельный compatibility package с явным versioning.

- [ ] **Поведение по умолчанию расходится между app-config и docker runtime**
   Файлы: `bot/src/main/resources/application.yml:12`, `docker/docker-compose.yml:61`, `docker/docker-compose.prod.yml:84`, `backend/src/main/resources/application.yaml:6`, `docker/docker-compose.yml:37`, `docker/docker-compose.prod.yml:48`.

   Проблема: ключевые runtime defaults (например, `BOT_ASYNC_PROCESSING` и имена/дефолты портов backend) отличаются в зависимости от того, как запускается сервис: напрямую через app config или через разные docker-compose файлы.

   Почему это плохо: одинаковый код ведёт себя по-разному между локальным запуском, compose-окружениями и продом. Это усложняет отладку и увеличивает риск "works on my machine" регрессий.

   Что делать: унифицировать defaults в одном месте и добавить startup-проверку, которая явно логирует эффективные значения критичных флагов.

## Minor

- [ ] **Нейминг и defaults разнесены по нескольким конфигам**
   Файлы: `README.md`, `bot/src/main/resources/application.yml`, `backend/src/main/resources/application.yaml`.

   Проблема: одинаковые сущности называются по-разному в документации, конфиге и коде.

   Почему это мелкий, но постоянный налог: приходится вспоминать, где именно лежит нужный default и как он называется в другом модуле.

   Что делать: унифицировать нейминг для общих понятий и вынести повторяющиеся defaults туда, где они реально используются.
