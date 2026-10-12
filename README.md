# 🎬 Interview Analyzer

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.x-3776AB.svg?style=flat-square&logo=python)](https://www.python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?style=flat-square&logo=docker)](https://www.docker.com/)

---

## 🛠 Технологический стек

- **Backend:** Java 21, Spring Boot 4.x, Maven
- **STT-сервис:** Python, FastAPI, faster-whisper (`large-v3-turbo`)
- **БД:** PostgreSQL 15 + Liquibase
- **Инфраструктура:** Docker Compose, ffmpeg, GitHub Actions

---

## 🔧 Локальное окружение и запуск

### Предварительные требования
* **Docker Desktop**
* **JDK 21**
* **Python 3.x**
* **ffmpeg** (на Mac: `brew install ffmpeg`)

Запускать нужно три компонента, строго в таком порядке: БД, Python-сервис, Java-приложение.

### 1. База данных (PostgreSQL)

В корне проекта:

```bash
docker compose up -d
```

*БД доступна на localhost:5432 (пользователь: postgres, пароль: postgres_password).*

### 2. Python STT-сервис

**Первый запуск (один раз):**

```bash
cd stt-service
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

**Запуск:**

```bash
cd stt-service
source venv/bin/activate
uvicorn main:app --reload --port 8000
```

Сервис готов, когда в логе появилось `Application startup complete`.
*При самом первом старте скачивается модель Whisper (~1.6 ГБ), это занимает несколько минут.*
*Документация API: http://localhost:8000/docs*

### 3. Java-приложение

В корне проекта, в новом окне терминала:

```bash
./mvnw spring-boot:run
```

*Приложение доступно по адресу: http://localhost:8080. При старте Liquibase сам применит миграции.*

---

## 🚀 Проверка работы

1. Загрузить видео:
   `POST http://localhost:8080/api/v1/media/upload`, Body → form-data, ключ `file` (тип File).
   В ответе придёт `id`.
2. Следить за статусом:
   `GET http://localhost:8080/api/v1/media/{id}`

Статусы проходят путь:
`UPLOADED → PROCESSING_AUDIO → AUDIO_EXTRACTED → TRANSCRIBING → TRANSCRIBED`
(при ошибке `FAILED`, причина в поле `errorMessage`). Когда статус `TRANSCRIBED`, в ответе появляется `transcript`.

---

## 🛑 Остановка

```bash
docker compose down   # остановить БД
```

Python-сервис и Java-приложение останавливаются через `Ctrl+C` в своих терминалах.