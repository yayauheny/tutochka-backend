# Анализ интеграции Backend и Telegram Bot

## Текущая ситуация

### Backend (tutochka-backend)
- **Технологии**: Kotlin 2.1.10 + Ktor 3.2.3
- **Порт**: 8080
- **Архитектура**: REST API с контроллерами, сервисами, репозиториями
- **DI**: Koin
- **База данных**: PostgreSQL + PostGIS + jOOQ

### Telegram Bot (tutochka-tg-bot)
- **Технологии**: Java 21 + Spring Boot 3.5.6
- **Порт**: 8081
- **Архитектура**: Spring Boot приложение с handlers, services, integration layer
- **DI**: Spring Framework
- **Интеграция**: REST клиент (RestClient) вызывает backend API

### Текущая интеграция
```java
// WebBackendClient использует RestClient для вызовов API
RestClient.builder()
    .baseUrl("http://localhost:8080/api/v1")
    .build()
```

---

## Варианты объединения

### Вариант 1: Multi-Module Gradle проект (РЕКОМЕНДУЕТСЯ) ⭐

#### Структура
```
tutochka/
├── settings.gradle.kts
├── build.gradle.kts (root)
├── backend/
│   ├── build.gradle.kts
│   └── src/... (существующий код)
├── bot/
│   ├── build.gradle.kts
│   └── src/... (существующий код)
└── shared/
    ├── build.gradle.kts
    └── src/... (общие DTOs, модели)
```

#### Преимущества
✅ **Чистое разделение** - каждый модуль независим  
✅ **Общие зависимости** - через version catalog  
✅ **Прямые вызовы** - bot может вызывать backend сервисы напрямую  
✅ **Легкое разделение** - можно собрать отдельные JAR'ы  
✅ **Единая сборка** - `./gradlew build` собирает все  
✅ **Общий код** - shared модуль для DTOs и моделей  

#### Недостатки
❌ Нужна миграция бота на Kotlin (опционально)  
❌ Сложнее настройка CI/CD (но решаемо)  

#### Сложность реализации: **Средняя** (2-3 дня)

---

### Вариант 2: Встроить Bot как модуль в Backend

#### Структура
```
tutochka-backend/
├── backend-api/ (существующий код)
├── telegram-bot/ (новый модуль)
└── shared/
```

#### Преимущества
✅ Backend остается основным проектом  
✅ Проще миграция (просто добавляем модуль)  

#### Недостатки
❌ Смешивание технологий (Kotlin + Java)  
❌ Сложнее разделить в будущем  
❌ Нужно решать конфликты зависимостей (Ktor vs Spring Boot)  

#### Сложность реализации: **Высокая** (4-5 дней)

---

### Вариант 3: Monorepo с отдельными приложениями

#### Структура
```
tutochka-monorepo/
├── backend/ (отдельный проект)
├── bot/ (отдельный проект)
└── shared/ (общая библиотека)
```

#### Преимущества
✅ Полная независимость проектов  
✅ Легко разделить на разные репозитории  
✅ Каждый проект может иметь свой CI/CD  

#### Недостатки
❌ Все еще REST вызовы между сервисами  
❌ Сложнее синхронизация версий  
❌ Нужны отдельные деплои  

#### Сложность реализации: **Низкая** (1 день)

---

## Рекомендуемое решение: Multi-Module Gradle проект

### Архитектура

```
tutochka/
├── settings.gradle.kts
├── build.gradle.kts
├── libs.versions.toml
├── gradle.properties
│
├── backend/                    # REST API модуль
│   ├── build.gradle.kts
│   └── src/main/kotlin/...
│
├── bot/                        # Telegram Bot модуль
│   ├── build.gradle.kts
│   └── src/main/java/...      # или kotlin/ после миграции
│
└── shared/                     # Общие модели и DTOs
    ├── build.gradle.kts
    └── src/main/kotlin/...
        └── dto/
            ├── RestroomResponseDto.kt
            └── NearestRestroomResponseDto.kt
```

### План миграции

#### Этап 1: Подготовка структуры (1 день)
1. Создать новый корневой проект `tutochka`
2. Настроить `settings.gradle.kts` с модулями
3. Перенести `backend` как модуль
4. Перенести `bot` как модуль
5. Создать `shared` модуль для общих DTOs

#### Этап 2: Настройка Gradle (1 день)
1. Настроить version catalog для общих зависимостей
2. Настроить зависимости между модулями:
   ```kotlin
   // bot/build.gradle.kts
   dependencies {
       implementation(project(":backend"))  // Прямой доступ к сервисам
       implementation(project(":shared"))     // Общие DTOs
   }
   ```
3. Настроить сборку отдельных JAR'ов

#### Этап 3: Рефакторинг интеграции (1 день)
1. Заменить `WebBackendClient` на прямые вызовы сервисов:
   ```kotlin
   // Вместо REST вызова
   val restrooms = restroomService.findNearest(lat, lon, limit)
   ```
2. Перенести общие DTOs в `shared` модуль
3. Обновить зависимости

#### Этап 4: Тестирование и оптимизация (1 день)
1. Протестировать интеграцию
2. Оптимизировать производительность
3. Обновить документацию

---

## Детальный план реализации

### 1. Создание структуры проекта

```bash
# Создать новый корневой проект
mkdir tutochka
cd tutochka

# Инициализировать Git
git init

# Создать структуру модулей
mkdir -p backend/src/main/kotlin
mkdir -p bot/src/main/java
mkdir -p shared/src/main/kotlin
```

### 2. Настройка settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "tutochka"

include("backend")
include("bot")
include("shared")

project(":backend").projectDir = file("backend")
project(":bot").projectDir = file("bot")
project(":shared").projectDir = file("shared")
```

### 3. Корневой build.gradle.kts

```kotlin
plugins {
    kotlin("jvm") version "2.2.20" apply false
    id("io.ktor.plugin") version "3.2.3" apply false
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "by.yayauheny"
    version = "0.1.0"
}
```

### 4. Backend модуль (build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktorPlugin)
    alias(libs.plugins.serializationPlugin)
    // ... остальные плагины
}

dependencies {
    implementation(project(":shared"))
    // ... существующие зависимости
}
```

### 5. Bot модуль (build.gradle.kts)

```kotlin
plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Прямой доступ к backend сервисам
    implementation(project(":backend"))
    implementation(project(":shared"))
    
    // Spring Boot зависимости
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.telegram:telegrambots:6.9.7.1")
}
```

### 6. Shared модуль (build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serializationPlugin)
}

dependencies {
    // Минимальные зависимости для сериализации
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

---

## Рефакторинг интеграции

### До (REST вызов)
```java
@Component
public class WebBackendClient implements BackendClient {
    private final RestClient client;
    
    public List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit) {
        return client.get()
            .uri("/restrooms/nearest?lat={lat}&lon={lon}&limit={limit}", lat, lon, limit)
            .retrieve()
            .body(NearestRestroomResponseDto[].class);
    }
}
```

### После (прямой вызов)
```java
@Component
public class BackendServiceClient implements BackendClient {
    private final RestroomService restroomService;  // Из backend модуля
    
    public BackendServiceClient(RestroomService restroomService) {
        this.restroomService = restroomService;
    }
    
    public List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit) {
        return restroomService.findNearest(lat, lon, limit);
    }
}
```

Или еще лучше - использовать сервисы напрямую:
```java
@Service
public class SearchService {
    private final RestroomService restroomService;  // Прямой доступ
    
    public List<Toilet> searchNearest(double lat, double lon, int limit) {
        return restroomService.findNearest(lat, lon, limit)
            .stream()
            .map(this::toToilet)
            .toList();
    }
}
```

---

## Преимущества прямых вызовов

### Производительность
- ❌ **REST**: ~5-10ms на запрос (сериализация, сеть, десериализация)
- ✅ **Прямой вызов**: <1ms (просто вызов метода)

### Надежность
- ❌ **REST**: Зависимость от сетевого стека, возможны таймауты
- ✅ **Прямой вызов**: Нет сетевых проблем

### Отладка
- ❌ **REST**: Нужно логировать HTTP запросы
- ✅ **Прямой вызов**: Обычный stack trace

---

## Разделение на отдельные сервисы в будущем

### Стратегия разделения

#### Вариант A: Оставить как есть
Если производительность достаточна, можно оставить multi-module структуру.

#### Вариант B: Разделить на микросервисы
1. **Создать API Gateway модуль**:
   ```kotlin
   // gateway/build.gradle.kts
   dependencies {
       implementation(project(":backend"))
       implementation(project(":bot"))
   }
   ```

2. **Использовать feature flags**:
   ```kotlin
   if (config.mode == "monolith") {
       // Прямые вызовы
       botService.search(restroomService)
   } else {
       // REST вызовы
       botService.search(restroomClient)
   }
   ```

3. **Создать отдельные Docker образы**:
   ```dockerfile
   # backend/Dockerfile
   FROM gradle:8.5-jdk21 AS builder
   WORKDIR /app
   COPY backend/ ./backend/
   RUN gradle :backend:build
   
   # bot/Dockerfile  
   FROM gradle:8.5-jdk21 AS builder
   WORKDIR /app
   COPY bot/ ./bot/
   RUN gradle :bot:build
   ```

#### Вариант C: Использовать Spring Cloud / Ktor плагины
Для автоматического разделения можно использовать:
- Spring Cloud для service discovery
- Ktor plugins для межсервисной коммуникации

---

## Оценка сложности

### Миграция в multi-module: **Средняя** (2-3 дня)

#### День 1: Структура и Gradle
- ✅ Создать структуру модулей
- ✅ Настроить settings.gradle.kts
- ✅ Настроить build.gradle.kts для каждого модуля
- ✅ Перенести код в модули
- ⚠️ Решить конфликты зависимостей

#### День 2: Рефакторинг интеграции
- ✅ Создать shared модуль для DTOs
- ✅ Заменить REST клиент на прямые вызовы
- ✅ Обновить Spring конфигурацию
- ✅ Протестировать интеграцию

#### День 3: Оптимизация и документация
- ✅ Оптимизировать производительность
- ✅ Обновить документацию
- ✅ Настроить CI/CD для multi-module

---

## Рекомендации

### ✅ Рекомендуется: Multi-Module подход

**Причины:**
1. **Производительность** - убираем сетевые задержки
2. **Простота** - один проект, одна сборка
3. **Гибкость** - легко разделить при необходимости
4. **Масштабируемость** - можно добавить больше модулей

### ⚠️ Важные моменты

1. **Миграция бота на Kotlin** (опционально):
   - Упростит интеграцию
   - Единый язык в проекте
   - Можно делать постепенно

2. **Общие DTOs в shared модуле**:
   - Избежать дублирования
   - Единая версия моделей
   - Проще синхронизация

3. **Конфигурация для разделения**:
   - Использовать feature flags
   - Подготовить Docker образы заранее
   - Документировать процесс разделения

---

## Следующие шаги

1. ✅ Создать PRD для миграции
2. ✅ Создать задачи в Task Master
3. ✅ Начать с создания структуры модулей
4. ✅ Постепенно переносить код
5. ✅ Тестировать на каждом этапе

---

## Вопросы для обсуждения

1. **Мигрировать бота на Kotlin сразу или позже?**
   - Сейчас: Java + Spring Boot работает
   - Потом: Kotlin упростит интеграцию

2. **Нужен ли shared модуль сразу?**
   - Да: избежим дублирования DTOs
   - Можно начать без него и добавить позже

3. **Какой порт использовать для объединенного приложения?**
   - Вариант 1: Backend на 8080, Bot на 8081 (как сейчас)
   - Вариант 2: Один порт с разными путями (/api/* и /telegram/*)
