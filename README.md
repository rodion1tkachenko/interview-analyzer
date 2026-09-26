# 🎬 Interview Analyzer (Career Copilot)

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Core-red.svg?style=flat-square&logo=liquibase)](https://www.liquibase.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?style=flat-square&logo=docker)](https://www.docker.com/)

Интеллектуальный бэкенд-сервис для автоматического анализа видеозаписей IT-собеседований. Система извлекает аудиодорожки из файлов кандидатов, транскрибирует речь, размечает диалог по спикерам и проводит детальную AI-оценку ответов кандидатa с помощью LLM.

---

## 🚀 Основные возможности (MVP)

- 📥 **Загрузка и обработка медиа:** Поддержка тяжелых видео и аудиофайлов (`.mov`, `.mp4`, `.mp3` до 1 ГБ).
- 🎙️ **Извлечение и оптимизация аудио:** Конвертация медиапотоков через `FFmpeg` для передачи в ASR-модели.
- 🗣️ **Транскрибация и диаризация:** Распознавание речи и разделение спикеров (интервьюер / кандидат) с помощью Whisper.
- 🤖 **AI-анализ ответов (LLM):** Автоматическая оценка правильности ответов, выявление сильных сторон, пропущенных ключевых поинтов и выставление балла (1–10).
- 📊 **Структурированный отчет:** Формирование подробного фидбека по каждому вопросу интервью.

---

## 🛠 Технологический стек

### Бэкенд & База данных
* **Language:** Java 21 (LTS) — *Project Loom (Virtual Threads), Pattern Matching, Records*
* **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA)
* **Database:** PostgreSQL 15
* **Migrations:** Liquibase
* **Build Tool:** Apache Maven

### Внешние инструменты & Интеграции
* **Media Processing:** FFmpeg
* **ASR & AI Engine:** OpenAI Whisper / LLM API
* **Infrastructure:** Docker & Docker Compose
* **CI/CD:** GitHub Actions (автоматическая сборка и проверка PR)

---

## 📂 Архитектура базы данных

Схема данных включает две ключевые сущности:

1. **`interviews`** — карточка проведенного собеседования (статус обработки, путь к исходному файлу, название).
2. **`questions`** — аналитика по каждому заданному вопросу (текст вопроса, ответ кандидата, оценка от 1 до 10, плюсы и упущенные моменты).

Управление миграциями осуществляется через **Liquibase** (`src/main/resources/db/changelog`).

---

## 🔧 Локальное окружение и запуск

### Предварительные требования
* Installed **Docker Desktop**
* Installed **JDK 21**
* Installed **FFmpeg** *(для локальной отладки конвертации)*

### 1. Запуск инфраструктуры (PostgreSQL)

В корне проекта выполните команду для поднятия контейнера с БД:

docker compose up -d
База данных будет доступна по адресу localhost:5432 (пользователь: postgres, пароль: postgres_password).
2. Запуск приложения
Скомпилируйте и запустите Spring Boot сервис:
./mvnw spring-boot:run

После старта приложение доступно по адресу: http://localhost:8080
📋 Текущий статус разработки (Roadmap)
[x] Инициализация проекта на Java 21 и Spring Boot 3.x
[x] Настройка Docker Compose и контейнера PostgreSQL
[x] Подключение Liquibase и написание стартовых SQL-миграций (V1__init_schema)
[x] Настройка CI-пайплайна в GitHub Actions
[ ] [В процессе] Реализация MediaController для загрузки .mov/.mp4 и интеграция с FFmpeg
[ ] Интеграция с Whisper для ASR и диаризации
[ ] Подключение LLM для генерации аналитических отчетов по собеседованию

