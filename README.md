# 🎬 Interview Analyzer

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Core-red.svg?style=flat-square&logo=liquibase)](https://www.liquibase.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?style=flat-square&logo=docker)](https://www.docker.com/)

Интеллектуальный бэкенд-сервис для автоматического анализа видеозаписей IT-собеседований. Система предназначается для загрузки медиафайлов кандидатов, транскрибации речи и последующего проведения AI-оценки ответов.

---

## 🛠 Технологический стек

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA)
- **Database:** PostgreSQL 15
- **Migrations:** Liquibase
- **Build Tool:** Apache Maven
- **Infrastructure:** Docker & Docker Compose
- **CI/CD:** GitHub Actions (автоматическая сборка при PR)

---

## 🔧 Локальное окружение и запуск

### Предварительные требования
* **Docker Desktop**
* **JDK 21**

### 1. Запуск инфраструктуры (PostgreSQL)

В корне проекта выполните команду для поднятия контейнера с БД:

docker compose up -d

*База данных будет доступна по адресу localhost:5432 (пользователь: postgres, пароль: postgres_password).*

### 2. Запуск приложения

Скомпилируйте и запустите Spring Boot сервис:

./mvnw spring-boot:run

После старта приложение доступно по адресу: http://localhost:8080

---

## 📋 Текущий статус разработки (Roadmap)

- [x] Инициализация проекта на Java 21 и Spring Boot 3.x
- [x] Настройка Docker Compose и контейнера PostgreSQL
- [x] Подключение Liquibase и написание стартовых SQL-миграций (V1__init_schema)
- [x] Настройка CI-пайплайна в GitHub Actions
- [ ] **[В процессе]** Реализация MediaController для загрузки .mov/.mp4 и интеграция с FFmpeg
- [ ] Транскрибация и распознавание речи
- [ ] Модуль AI-аналитики ответов
