# Отслеживание прогресса выполнения улучшений

## Дата начала: 2025-01-XX
## Выбранный план: Вариант B (Сбалансированный) + Кэширование

---

## Принятые решения

✅ **Язык ошибок:** Русский (оставляем как есть)  
✅ **План выполнения:** Вариант B (критичные + важные)  
✅ **Структура пакетов:** Отложена на потом  
✅ **Кэширование:** Включить с конфигурацией и флагом включения/выключения  
✅ **Тестирование:** Покрыть только мапперы, упростить интеграционные тесты  

---

## Статус выполнения

### Фаза 1: Критичные исправления (6-9 часов)

#### ✅ Этап 1.1: Транзакции (4-6 часов)
**Статус:** 🟡 В процессе  
**Прогресс:** 85%

**Задачи:**
- [x] Отключить auto-commit в `DatabaseConfig.kt`
- [x] Создать extension функцию `transactionSuspend` в `util/TransactionExtensions.kt`
- [x] Обновить `RestroomRepositoryImpl.save()` - обернуть в транзакцию
- [x] Обновить `RestroomRepositoryImpl.update()` - обернуть в транзакцию
- [x] Обновить `RestroomRepositoryImpl.deleteById()` - обернуть в транзакцию
- [x] Обновить `CityRepositoryImpl.save()` - обернуть в транзакцию
- [x] Обновить `CityRepositoryImpl.update()` - обернуть в транзакцию
- [x] Обновить `CityRepositoryImpl.deleteById()` - обернуть в транзакцию
- [x] Обновить `CountryRepositoryImpl.save()` - обернуть в транзакцию
- [x] Обновить `CountryRepositoryImpl.update()` - обернуть в транзакцию
- [x] Обновить `CountryRepositoryImpl.deleteById()` - обернуть в транзакцию
- [ ] Запустить все тесты, убедиться что ничего не сломалось
- [ ] Добавить тест на атомарность транзакции (rollback при ошибке)

**Файлы:**
- `src/main/kotlin/yayauheny/by/config/DatabaseConfig.kt`
- `src/main/kotlin/yayauheny/by/util/TransactionExtensions.kt` (новый)
- `src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CityRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CountryRepositoryImpl.kt`

---

#### ✅ Этап 1.2: Обработка ошибок (2-3 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Создать `RepositoryException` и `EntityNotFoundException` в `common/errors/`
- [ ] Заменить `error()` на исключения в `RestroomRepositoryImpl`
- [ ] Заменить `error()` на исключения в `CityRepositoryImpl`
- [ ] Заменить `error()` на исключения в `CountryRepositoryImpl`
- [ ] Расширить обработку `PSQLException` в `ErrorHandlingPlugin.kt` (все SQL состояния)
- [ ] Добавить обработку новых исключений в `ErrorHandlingPlugin.kt`
- [ ] Обновить тесты для проверки новых исключений

**Файлы:**
- `src/main/kotlin/yayauheny/by/common/errors/RepositoryException.kt` (новый)
- `src/main/kotlin/yayauheny/by/common/errors/RestExceptions.kt`
- `src/main/kotlin/yayauheny/by/common/plugins/ErrorHandlingPlugin.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CityRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CountryRepositoryImpl.kt`

---

### Фаза 2: Важные улучшения (20-30 часов)

#### ✅ Этап 2.1: Устранение дублирования кода (2-3 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Создать extension функцию `selectWithCoordinates()` в `util/RepositoryExtensions.kt`
- [ ] Обновить `RestroomRepositoryImpl.findAll()` - использовать новую функцию
- [ ] Обновить `RestroomRepositoryImpl.findById()` - использовать новую функцию
- [ ] Обновить `RestroomRepositoryImpl.findSingle()` - использовать новую функцию
- [ ] Обновить `RestroomRepositoryImpl.findByCityId()` - использовать новую функцию
- [ ] Аналогично для `CityRepositoryImpl` если нужно
- [ ] Запустить тесты

**Файлы:**
- `src/main/kotlin/yayauheny/by/util/RepositoryExtensions.kt` (новый или существующий)
- `src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CityRepositoryImpl.kt`

---

#### ✅ Этап 2.2: Оптимизация fetchCount (1-2 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Проверить все вызовы `executePaginated()` в репозиториях
- [ ] Определить где можно отключить `fetchCount` (например, для `/nearest` если будет пагинация)
- [ ] Добавить параметр `fetchCount = false` где уместно
- [ ] Запустить тесты

**Файлы:**
- `src/main/kotlin/yayauheny/by/repository/impl/RestroomRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CityRepositoryImpl.kt`
- `src/main/kotlin/yayauheny/by/repository/impl/CountryRepositoryImpl.kt`

---

#### ✅ Этап 2.3: Расширение валидации (2-3 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Создать `ApiConstants.kt` с константами
- [ ] Расширить `validateRestroomOnCreate` - добавить валидацию всех полей
- [ ] Расширить `validateRestroomOnUpdate` - добавить валидацию всех полей
- [ ] Расширить `validateCityOnCreate` - добавить валидацию длины строк
- [ ] Расширить `validateCountryOnCreate` - добавить валидацию длины строк
- [ ] Обновить unit тесты валидации
- [ ] Упростить интеграционные тесты - убрать лишние проверки валидации

**Файлы:**
- `src/main/kotlin/yayauheny/by/config/ApiConstants.kt` (новый)
- `src/main/kotlin/yayauheny/by/service/validation/RestroomValidators.kt`
- `src/main/kotlin/yayauheny/by/service/validation/CityValidators.kt`
- `src/main/kotlin/yayauheny/by/service/validation/CountryValidators.kt`
- `src/test/kotlin/yayauheny/by/unit/service/ValidationTest.kt`
- `src/test/kotlin/yayauheny/by/integrationTest/kotlin/api/**/*.kt` (упростить)

---

#### ✅ Этап 2.4: Улучшение обработки ошибок БД (2-3 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Расширить обработку `PSQLException` в `ErrorHandlingPlugin.kt`
- [ ] Добавить обработку всех SQL состояний (23503, 23514, 23502, 42P01)
- [ ] Добавить структурированное логирование с контекстом
- [ ] Обновить тесты для проверки различных ошибок БД

**Файлы:**
- `src/main/kotlin/yayauheny/by/common/plugins/ErrorHandlingPlugin.kt`
- `src/test/kotlin/yayauheny/by/unit/controller/HttpStatusCodesTest.kt`

---

#### ✅ Этап 2.5: Тесты для мапперов (3-4 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Создать `RestroomMapperTest.kt`
- [ ] Добавить тесты для `mapFromRecord()` - все поля
- [ ] Добавить тесты для `applyUpdateDto()` - все варианты
- [ ] Добавить тесты для `mapToNearestRestroom()` - все поля
- [ ] Создать `CityMapperTest.kt`
- [ ] Создать `CountryMapperTest.kt`
- [ ] Запустить все тесты

**Файлы:**
- `src/test/kotlin/yayauheny/by/unit/mapper/RestroomMapperTest.kt` (новый)
- `src/test/kotlin/yayauheny/by/unit/mapper/CityMapperTest.kt` (новый)
- `src/test/kotlin/yayauheny/by/unit/mapper/CountryMapperTest.kt` (новый)

---

### Фаза 3: Дополнительные улучшения

#### ✅ Этап 3.1: Кэширование (3-4 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Добавить зависимость Caffeine в `libs.versions.toml`
- [ ] Создать `CacheConfig.kt` с конфигурацией кэша
- [ ] Добавить флаг включения/выключения кэша в `application.yaml` и `DatabaseConfig`
- [ ] Создать `CountryCache` и `CityCache` с условной инициализацией
- [ ] Обновить `CountryService` для использования кэша (если включен)
- [ ] Обновить `CityService` для использования кэша (если включен)
- [ ] Добавить инвалидацию кэша при изменениях
- [ ] Добавить тесты для кэширования

**Файлы:**
- `libs.versions.toml`
- `src/main/kotlin/yayauheny/by/config/CacheConfig.kt` (новый)
- `src/main/kotlin/yayauheny/by/config/DatabaseConfig.kt`
- `src/main/resources/application.yaml`
- `src/main/kotlin/yayauheny/by/service/CountryService.kt`
- `src/main/kotlin/yayauheny/by/service/CityService.kt`
- `src/main/kotlin/yayauheny/by/di/ServiceModule.kt`

---

#### ✅ Этап 3.2: Health checks (2-3 часа)
**Статус:** 🔴 Не начато  
**Прогресс:** 0%

**Задачи:**
- [ ] Создать `HealthController.kt`
- [ ] Добавить `/health` endpoint
- [ ] Добавить `/health/ready` endpoint
- [ ] Добавить `/health/live` endpoint
- [ ] Добавить проверку БД (простой SELECT 1)
- [ ] Добавить тесты для health endpoints
- [ ] Зарегистрировать routes в `RoutingConfig.kt`

**Файлы:**
- `src/main/kotlin/yayauheny/by/controller/HealthController.kt` (новый)
- `src/test/kotlin/yayauheny/by/unit/controller/HealthControllerTest.kt` (новый)
- `src/main/kotlin/yayauheny/by/config/RoutingConfig.kt`

---

## Общая статистика

**Всего этапов:** 8  
**Завершено:** 0  
**В процессе:** 0  
**Не начато:** 8  

**Общий прогресс:** 10%  

**Оценка времени:**
- Запланировано: 26-39 часов
- Затрачено: ~1 час (Этап 1.1)
- Осталось: 25-38 часов

---

## Заметки и комментарии

### 2025-01-XX - Начало работы
- План утвержден
- Решения приняты
- Готов к началу выполнения

### 2025-01-XX - Этап 1.1 завершен
- ✅ Отключен auto-commit в DatabaseConfig
- ✅ Создана extension функция transactionSuspend
- ✅ Все методы save/update/deleteById обернуты в транзакции
- ✅ Компиляция проходит успешно
- ⏳ Осталось: запустить тесты и добавить тест на атомарность

---

## Легенда статусов

- 🔴 Не начато
- 🟡 В процессе
- 🟢 Завершено
- ⚠️ Проблемы/Блокеры
