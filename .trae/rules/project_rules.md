---
description: Kotlin Ktor backend with YouTrack-style task flow, atomic commits, and AI task reports
globs:
  - "src/**/*.kt"
  - "src/**/*.kts"
  - "build.gradle.kts"
  - "settings.gradle.kts"
  - "gradle/**/*.kts"
  - "src/test/**/*.kt"
alwaysApply: true
---

# Project Rules – Kotlin Ktor Backend + Bug Tracker (“Жук”)

These rules define how Trae AI (Chat, Builder, Builder with MCP) must work in this project:
- Follow this document before using auto-completion, bulk edits, or automated Builder flows.
- Keep changes small, testable, and tied to individual tasks from the bug tracker (“Жук” / YouTrack/Jira-style tickets).

---

## Core Principles

- Follow **SOLID**, **DRY**, **KISS**, **YAGNI**.
- Prefer simple, explicit solutions over clever abstractions.
- Adhere to **OWASP** security best practices for backend services.
- Break every feature request into **smallest coherent tasks** and solve them **step-by-step**.
- Keep changesets **narrow and focused** so each Git commit corresponds to exactly one completed task.

---

## Technology Stack

Use these as the default choices unless the user explicitly says otherwise.

- **Language & Runtime**
    - Kotlin **2.1+**
    - JDK **21 (LTS)**
- **Framework**
    - Backend: **Ktor 3.x** (server-side)
- **Build**
    - Gradle with **Kotlin DSL**
- **Libraries / Dependencies**
    - Ktor Server Core / Netty
    - `kotlinx.serialization`
    - **Exposed** (SQL DSL / ORM)
    - **PostgreSQL + PostGIS**
    - **Liquibase** for migrations
    - **Testcontainers** for integration tests
    - **Koin** for dependency injection
    - **Logback** for logging

When adding new libs:
- Check compatibility with Kotlin 2.1 + JDK 21.
- Prefer widely used, actively maintained libraries.
- Keep dependencies minimal and justify each new one in the PR / commit message if significant.

---

## Project Structure

Keep the existing layered structure. Do **not** force a feature-based folder structure unless the user explicitly asks.

