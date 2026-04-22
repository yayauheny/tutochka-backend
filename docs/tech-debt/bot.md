# Техдолг bot

Список покрывает только Telegram bot runtime, HTTP-интеграцию, кеш, форматирование и webhook-слой. Контрактная проблема с общими DTO и enum-ами вынесена в `general.md`, чтобы не дублировать архитектурный вывод.

## Critical

- [ ] **Webhook принимает обновления без аутентификации**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/ingress/WebhookController.java:21-23`, `bot/src/main/java/by/yayauheny/tutochkatgbot/config/WebhookRegistrar.java:36-41`, `bot/src/main/resources/application.yml:5-15`.

   Проблема: контроллер принимает любой POST на `${telegram.bot.webhook-path}` и сразу передаёт update дальше. При регистрации вебхука задаётся только URL, без secret token или другого inbound-auth.

   Почему это критично: если endpoint доступен извне, любой клиент может подсовывать боту произвольные обновления. Это прямой путь к несанкционированным командам, ложным callback-ам и спаму.

   Что делать: добавить проверку секретного токена Telegram или другой защиты входящего запроса, а регистрацию webhook синхронизировать с этим токеном. Без этого вебхук нельзя считать безопасным.

## Medium

- [ ] **Callback protocol остался stringly-typed и хрупким**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/callback/CallbackData.java:10-43`, `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/UpdateContext.java:10-118`.

   Проблема: callback data кодируется через строковые префиксы и двоеточия, а парсинг строится на `startsWith`, `split` и ручных helper-ах. В файле уже есть transitional методы вроде `routeProvider` и `routeRestroomId`.

   Почему это плохо: формат не версионирован, не экранирует значения и легко ломается при рефакторинге или появлении новых значений. Ошибки видны только в рантайме.

   Что делать: ввести типизированный payload для callback-ов с явным encoder/decoder и версией схемы. Старый формат лучше мигрировать один раз, а не поддерживать бесконечно.

- [ ] **UpdateRouter смешивает роутинг, обработку ошибок и пользовательские fallback-ы**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/router/UpdateRouter.java:44-137`, `bot/src/main/java/by/yayauheny/tutochkatgbot/service/UpdateHandlingService.java:24-31`, `bot/src/main/java/by/yayauheny/tutochkatgbot/service/UpdateProcessingService.java:28-34`, `bot/src/main/java/by/yayauheny/tutochkatgbot/service/AsyncUpdateHandlingService.java:18-21`.

   Проблема: роутизм, политика ошибок и ответ пользователю переплетены в одном месте. В каждой ветке ловится generic `Exception`, а сверху добавлены ещё две обёртки, которые не несут отдельной бизнес-логики.

   Почему это плохо: поток обработки update сложно проследить, а тестировать негативные сценарии неудобно. Из-за широких `catch`-блоков разные ошибки превращаются в одинаковое "что-то пошло не так".

   Что делать: вынести policy for errors в один слой, использовать доменные исключения или result-типы и убрать лишнюю обёртку, если она не даёт отдельной ценности.

- [ ] **FormatterService стал god class для всего текста и HTML**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/service/FormatterService.java:20-293`.

   Проблема: один сервис выбирает имя, адрес, расписание, теги, ориентиры, метро, HTML-разметку и fallback-логику. Внутри есть raw `Map<String, Object>` доступ к backend-данным, а внизу лежат мёртвые helper-методы `formatBuildingLine`, `formatSubwayLine`, `formatScheduleBlock`, `formatNoteBlock`, `formatRouteBlock`.

   Почему это плохо: любой форматный баг затрагивает сразу весь экранный вывод. Такой класс тяжело читать, сложно тестировать и ещё сложнее переписывать без регрессий.

   Что делать: разбить на отдельные рендереры для списка, карточки и тегов, а raw map-вход заменить на более строгую модель расписания и атрибутов.

- [ ] **BackListSnapshotCache держит одно и то же состояние в двух структурах**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/cache/BackListSnapshotCache.java:19-154`.

   Проблема: используется и Caffeine cache, и отдельный `ConcurrentHashMap states`. Логика expiry, removal listener и metrics вынужденно синхронизирует два представления одного и того же снапшота.

   Почему это плохо: двойное состояние всегда усложняет reasoning about cache. Любая правка TTL, eviction или метрик может оставить структуры несогласованными.

   Что делать: оставить один источник состояния и один путь expiry. Если нужны метрики или маркеры устаревания, хранить их вместе со снапшотом, а не в параллельной карте.

- [ ] **SearchService повторяет работу backend и прячет часть ошибок**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/service/SearchService.java:29-76`.

   Проблема: после вызова backend бот ещё раз фильтрует и сортирует nearest results. В `getById` любые 4xx превращаются в `Optional.empty()`, из-за чего разные проблемы выглядят одинаково.

   Почему это плохо: бот дублирует уже реализованную серверную логику и теряет информацию о причине ошибки. Это повышает риск несовпадения поведения между backend и bot.

   Что делать: оставить ranking/pagination на backend, а в бот передавать уже нормализованный результат. Для `getById` лучше разделять "не найдено", "невалидный ID" и "ошибка backend".

- [ ] **WebBackendClient использует слишком жёсткий retry-паттерн**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/integration/WebBackendClient.java:32-108`.

   Проблема: retry policy задаётся жёстко и вручную, а ошибки HTTP обрабатываются через локальное разборчивание исключений. Это рабочее решение, но оно плохо масштабируется по поведению и наблюдаемости.

   Почему это плохо: при деградации backend бот может одинаково агрессивно ретраить разные типы ошибок и нагружать сервис ещё сильнее. Без jitter и circuit breaker поведение легко становится шумным.

   Что делать: вынести retry/backoff в отдельную политику, добавить jitter и чёткое разделение retryable/non-retryable ошибок.

- [ ] **BotSender смешивает отправку, форматирование и fallback-логику**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/bot/BotSender.java:39-92`.

   Проблема: `HTML` parse mode задан для всех текстов, а при ошибке редактирования сообщение просто отправляется заново. Это удобный fallback, но он не ограничен конкретными Telegram-ошибками.

   Почему это плохо: один и тот же пользовательский шаг может внезапно породить дубликат сообщения. Кроме того, общий `HTML`-режим заставляет каждый вызов помнить о корректном escaping.

   Что делать: разделить рендеринг и transport, использовать parse mode точечно и падать на ожидаемых ошибках осознанно, а не через универсальный fallback.

- [ ] **Async webhook path подтверждает update до обработки и теряет часть ошибок**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/service/UpdateProcessingService.java:28-34`, `bot/src/main/java/by/yayauheny/tutochkatgbot/service/AsyncUpdateHandlingService.java:18-21`, `bot/src/main/java/by/yayauheny/tutochkatgbot/config/AsyncConfig.java:17-25`.

   Проблема: при `bot.async-processing=true` контроллер быстро возвращает 200, а обработка уходит в `@Async void` без явной стратегии для uncaught/rejected ошибок.

   Почему это плохо: Telegram считает update доставленным, но часть ошибок может остаться только в логах и не иметь механизма повторной обработки.

   Что делать: добавить обработчик async-ошибок и rejection-policy, а для критичных операций рассмотреть подтверждение webhook только после минимально надёжной обработки.

- [ ] **UpdateContext не учитывает callback-ы без message**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/handler/UpdateContext.java:59-64`.

   Проблема: `fromCallbackQuery` без проверок обращается к `callbackQuery.getMessage()`.

   Почему это плохо: для callback-сценариев с `inline_message_id` (или других edge-case апдейтов без message) возможен NPE и деградация в generic fallback.

   Что делать: добавить защиту для callback-ов без message и отдельную ветку обработки с безопасным извлечением идентификаторов.

- [ ] **В карточке туалета почти не показывается расписание**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/service/FormatterService.java:105-131,269-276`, `bot/src/main/java/by/yayauheny/tutochkatgbot/messages/Messages.java:93-103`, `bot/src/main/java/by/yayauheny/tutochkatgbot/util/WorkTimeFormatter.java:22-59`.

   Проблема: текущий compact-template деталей не включает блок расписания, а методы `selectWorkTime/formatScheduleBlock` фактически не участвуют в итоговом ответе пользователю.

   Почему это плохо: пользователь часто не видит ключевую информацию "открыто/когда работает", даже если расписание есть в backend.

   Что делать: вернуть расписание в основной шаблон детали (с безопасным ограничением длины и fallback), а форматирование времени вынести в отдельный, покрытый тестами render-path.

## Minor

- [ ] **Messages содержит много transitional constant-ов**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/messages/Messages.java:9-24,62-91`.

   Проблема: рядом с актуальными строками остаются deprecated callbacks и старый шаблон деталей, который уже не должен быть основной точкой поддержки.

   Почему это мелкий долг: он не ломает runtime сразу, но постоянно мешает понимать, какой формат реально используется.

   Что делать: удалить deprecated-константы после завершения миграции и оставить только один путь построения текста.

- [ ] **UpdateProcessingService и AsyncUpdateHandlingService почти ничего не добавляют**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/service/UpdateProcessingService.java:13-34`, `bot/src/main/java/by/yayauheny/tutochkatgbot/service/AsyncUpdateHandlingService.java:11-21`.

   Проблема: слои существуют почти только ради переключателя async/sync и не содержат собственной логики.

   Почему это плохо: лишний уровень абстракции не даёт поведения, но добавляет шум в навигации по коду.

   Что делать: если async-путь не будет расширяться, свернуть это в один сервис или хотя бы упростить композицию.

- [ ] **AdminProperties silently drops invalid admin IDs**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/config/AdminProperties.java:12-30`.

   Проблема: невалидные значения просто исчезают из множества, без явной ошибки или хотя бы warning.

   Почему это плохо: опечатка в конфиге приводит к молчаливой потере доступа, а не к понятному fail-fast поведению.

   Что делать: валидировать список целиком и падать при невалидных значениях, либо логировать их явно.

- [ ] **Клавиатуры собираются вручную в нескольких местах**
   Файлы: `bot/src/main/java/by/yayauheny/tutochkatgbot/keyboard/InlineKeyboardFactory.java`, `bot/src/main/java/by/yayauheny/tutochkatgbot/keyboard/ReplyKeyboardFactory.java`.

   Проблема: много ручного branching и шаблонного кода для клавиатур.

   Почему это мелкий долг: не критично для работы, но увеличивает объём изменений при каждом новом button flow.

   Что делать: вынести небольшие builders и переиспользовать готовые варианты клавиатур там, где это возможно.

- [ ] **InMemorySessionStore остаётся только процессным хранилищем**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/session/InMemorySessionStore.java:17-64`.

   Проблема: состояние живёт только в памяти процесса и очищается фоновым scheduler-ом.

   Почему это плохо: после рестарта всё теряется, а горизонтальное масштабирование требует уже другого хранилища.

   Что делать: если сессии станут важнее, заменить процессную память на Redis или другой shared store.

- [ ] **UpdateRouter логирует полный Telegram Update в warn-ветке**
   Файл: `bot/src/main/java/by/yayauheny/tutochkatgbot/router/UpdateRouter.java:131`.

   Проблема: при отсутствии обработчика в лог пишется целиком объект `update`.

   Почему это плохо: в логах оказываются лишние пользовательские данные (текст, метаданные сообщений, иногда координаты), и быстро растёт шум.

   Что делать: логировать только безопасный набор полей (тип update, chatId/userId, handler outcome), а полные payload-ы оставлять только под точечный debug.
