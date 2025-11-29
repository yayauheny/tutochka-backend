# Анализ архитектурных проблем проекта

## 🔴 Критические проблемы

### 1. Валидация в сервисах вместо валидаторов

#### 1.1. CityService.createCity() - валидация поля `region`
**Файл:** `src/main/kotlin/yayauheny/by/service/CityService.kt:49-63`

**Проблема:**
```kotlin
// Валидация nullable полей на уровне сервиса
createDto.region?.let { region ->
    if (region.length < 2 || region.length > 255) {
        validationErrors.add(
            yayauheny.by.common.errors.FieldError(
                "region",
                if (region.length < 2) {
                    "Регион должен содержать минимум 2 символа"
                } else {
                    "Регион слишком длинный (максимум 255 символов)"
                }
            )
        )
    }
}
```

**Решение:** Перенести в `cityCreateValidator`:
```kotlin
val cityCreateValidator =
    Validation<CityCreateDto> {
        // ... existing validations ...
        CityCreateDto::region {
            ifPresent {
                minLength(2) hint "Регион должен содержать минимум 2 символа"
                maxLength(255) hint "Регион слишком длинный (максимум 255 символов)"
            }
        }
    }
```

**Комментарий в коде:** В `Validators.kt:73` есть комментарий `// region - nullable поле, валидация выполняется на уровне сервиса` - это неправильный подход.

---

### 2. Валидация в контроллерах

#### 2.1. CityController.get("/search") - валидация параметра `name`
**Файл:** `src/main/kotlin/yayauheny/by/controller/CityController.kt:49-59`

**Проблема:**
```kotlin
get("/search") {
    val name = call.request.queryParameters["name"]?.trim()
    val errors = mutableListOf<FieldError>()
    if (name.isNullOrBlank()) {
        errors += FieldError("name", "Параметр name обязателен")
    } else if (name.length < 2) {
        errors += FieldError("name", "Минимальная длина параметра name — 2 символа")
    }
    if (errors.isNotEmpty()) {
        throw ValidationRestException(errors = errors)
    }
    // ...
}
```

**Решение:** Создать валидатор для параметров поиска или перенести валидацию в сервис:
```kotlin
// Вариант 1: Валидатор
data class CitySearchParams(val name: String)

val citySearchParamsValidator = Validation<CitySearchParams> {
    CitySearchParams::name {
        minLength(2) hint "Минимальная длина параметра name — 2 символа"
        maxLength(255) hint "Параметр name слишком длинный"
    }
}

// Вариант 2: Валидация в сервисе
suspend fun findCitiesByName(name: String, pagination: PaginationRequest): PageResponse<CityResponseDto> {
    require(name.isNotBlank()) { "Параметр name обязателен" }
    require(name.length >= 2) { "Минимальная длина параметра name — 2 символа" }
    return cityRepository.findByName(name, pagination)
}
```

---

## 🟡 Средние проблемы

### 3. Нарушение слоев: использование FilterCriteria в сервисах

#### 3.1. CityService.createCity() - проверка дубликатов
**Файл:** `src/main/kotlin/yayauheny/by/service/CityService.kt:72-94`

**Проблема:** Сервис использует `FilterCriteria` и `FilterOperator` напрямую, что является деталями репозитория.

```kotlin
val existingByRu =
    cityRepository.findSingle(
        listOf(
            FilterCriteria("countryId", FilterOperator.EQ, createDto.countryId.toString()),
            FilterCriteria("nameRu", FilterOperator.EQ, createDto.nameRu)
        )
    )
```

**Решение:** Добавить специализированные методы в репозиторий:
```kotlin
// В CityRepository
suspend fun existsByCountryIdAndNameRu(countryId: UUID, nameRu: String): Boolean
suspend fun existsByCountryIdAndNameEn(countryId: UUID, nameEn: String): Boolean

// В CityService
val existingByRu = cityRepository.existsByCountryIdAndNameRu(createDto.countryId, createDto.nameRu)
val existingByEn = cityRepository.existsByCountryIdAndNameEn(createDto.countryId, createDto.nameEn)
```

#### 3.2. CountryService - аналогичная проблема
**Файл:** `src/main/kotlin/yayauheny/by/service/CountryService.kt:31-34`

**Проблема:**
```kotlin
val existing =
    countryRepository.findSingle(
        listOf(FilterCriteria("code", FilterOperator.EQ, createDto.code))
    )
```

**Решение:**
```kotlin
// В CountryRepository
suspend fun existsByCode(code: String): Boolean

// В CountryService
val existing = countryRepository.existsByCode(createDto.code)
```

---

### 4. Непоследовательная обработка ошибок

#### 4.1. RestroomService.createRestroom() - проверка cityId
**Файл:** `src/main/kotlin/yayauheny/by/service/RestroomService.kt:39-42`

**Проблема:** Используется `NotFoundRestException` для несуществующего `cityId`, но это валидация входных данных.

**Текущий код:**
```kotlin
createDto.cityId?.let { cityId ->
    cityRepository.findById(cityId)
        ?: throw NotFoundRestException("City with id $cityId not found")
}
```

**Решение:** Использовать `BadRequestRestException` (как в CityService):
```kotlin
createDto.cityId?.let { cityId ->
    cityRepository.findById(cityId)
        ?: throw BadRequestRestException("City with id $cityId not found")
}
```

---

## 🟢 Мелкие улучшения

### 5. Дублирование логики проверки существования

#### 5.1. Повторяющиеся паттерны в сервисах
Все сервисы имеют похожую логику проверки существования ресурсов перед созданием. Можно вынести в базовый класс или утилиту, но это не критично.

---

### 6. Валидация nullable полей в валидаторах

#### 6.1. RestroomCreateDto - name и description
**Файл:** `src/main/kotlin/yayauheny/by/service/validation/Validators.kt:100`

**Комментарий:** `// name и description - nullable поля, валидация выполняется на уровне сервиса при необходимости`

**Проблема:** Если валидация нужна, она должна быть в валидаторе с `ifPresent`.

**Решение:** Если валидация не нужна - удалить комментарий. Если нужна - добавить:
```kotlin
RestroomCreateDto::name {
    ifPresent {
        minLength(1) hint "Название не может быть пустым"
        maxLength(255) hint "Название слишком длинное"
    }
}
RestroomCreateDto::description {
    ifPresent {
        maxLength(1000) hint "Описание слишком длинное (максимум 1000 символов)"
    }
}
```

---

## 📋 Приоритеты исправления

1. **Высокий приоритет:**
   - ✅ Перенести валидацию `region` из CityService в валидатор
   - ✅ Убрать валидацию из CityController.get("/search")
   - ✅ Исправить RestroomService.createRestroom() - использовать BadRequestRestException

2. **Средний приоритет:**
   - Добавить специализированные методы в репозитории (existsBy*)
   - Убрать использование FilterCriteria из сервисов

3. **Низкий приоритет:**
   - Добавить валидацию nullable полей в валидаторы (если необходимо)
   - Рефакторинг дублирующейся логики

---

## 📝 Резюме

**Основные проблемы:**
1. Валидация находится в сервисах и контроллерах вместо валидаторов
2. Сервисы используют детали репозитория (FilterCriteria) напрямую
3. Непоследовательная обработка ошибок валидации

**Рекомендации:**
- Все валидации должны быть в валидаторах
- Сервисы должны использовать высокоуровневые методы репозиториев
- Контроллеры должны только делегировать вызовы сервисам
