# FlightBookingSystem

Booking flight ticket system.

Forked from https://github.com/aliahmadi4/FlightBookingSystem
Many thanks to the authors of the original code.

1. Framework: Spring Boot
2. Database: MySQL
3. JAVA: 21

Hibernate, Thymeleaf, Spring Boot Security, Thymeleaf Dialect, JPA, API Docs

Roles:

1. Admin: username=john, password:john123, Add/Remove flight, airplane, and aircraft, search flight, verify ticket
2. Agent: username=mike, password:mike123, Book/Cancel ticket for passengers, search flight, verify ticket

---

# Быстрый старт через Docker

1. Скопируйте переменные окружения и при необходимости скорректируйте значения:

   ```bash
   cp .env.example .env
   ```

   _`.env` подключается к обоим контейнерам через `env_file`, поэтому держите файл рядом с `docker-compose.yml` и заполните все переменные (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MYSQL\__`, `JWT\__`). При отсутствии значений контейнеры запустятся, но приложение упадёт с понятной ошибкой — сразу видно, что конфиг не подхватился._

2. Соберите и поднимите стек приложений (Spring Boot + MySQL) одной командой:

   ```bash
   docker compose up --build
   ```

   Перед первым запуском по желанию создайте директорию `database/mysql_data` (если её нет, Compose создаст автоматически) — там будут храниться данные MySQL.

   Первичный запуск импортирует дамп `database/ftb.sql` в контейнер MySQL; приложение автоматически поднимется после прохождения healthcheck СУБД.

   _Если меняете `MYSQL_USER`/`MYSQL_PASSWORD`, удалите `database/mysql_data`, чтобы MySQL пересоздался с новыми учетными данными._

3. После старта API доступен на `http://localhost:8080`, база — на `localhost:3306` (учётные данные из `.env`, в примере `root`/`root`).

   Чтобы остановить окружение, выполните `docker compose down`; для полного сброса БД удалите директорию `database/mysql_data`.

# HOW-TOs

1. Access REST API Docs:

> host:port/api-docs/

e.g.

> http://localhost:8080/api-docs/

Swagger-UI (human-friendly API Docs) should be available at:

> host:port/swagger-ui.html

e.g.

> http://localhost:8080/swagger-ui.html

---

# TODO:

- UI: (Admin) Flights.
- Cleanup documentation.
- Add more unit and integration tests.
