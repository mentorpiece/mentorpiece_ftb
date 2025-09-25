# Flight Ticket Booking System

A comprehensive flight ticket booking system built with Spring Boot, featuring role-based authentication, REST API, and modern web interface.

**Version:** 1.0.0  
**Forked from:** https://github.com/aliahmadi4/FlightBookingSystem  
Many thanks to the authors of the original code.

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.3.4
- **Java Version:** 21 (LTS)
- **Database:** MySQL/MariaDB compatible
- **Security:** Spring Security with JWT support
- **Frontend:** Thymeleaf + Bootstrap
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Build Tool:** Maven with wrapper

## 🚀 Features

### Core Functionality

- ✅ Flight search and booking
- ✅ Aircraft and airport management
- ✅ User role management (USER, AGENT, ADMIN)
- ✅ CSV import for aircraft data
- ✅ Flight status and gate management

### Security Features

- ✅ Role-based access control
- ✅ Session-based authentication (web)
- ✅ JWT Bearer token authentication (API)
- ✅ Dynamic role switching
- ✅ Environment variable configuration

### API Features

- ✅ RESTful API with OpenAPI documentation
- ✅ Swagger UI integration
- ✅ JSON Patch support for partial updates
- ✅ Comprehensive error handling

## 👥 User Roles

### 🔵 USER Role

- Search flights
- View flight information
- Limited UI access (Aircraft/Airport menus hidden)

### 🟡 AGENT Role

- All USER permissions
- Book/cancel tickets for passengers
- Manage flight bookings
- Access to booking interfaces

### 🔴 ADMIN Role

- All AGENT permissions
- Add/remove flights, aircraft, and airports
- Full CRUD operations via API
- Access to all management interfaces

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use included wrapper)
- Docker & Docker Compose (for containerised setup)
- MySQL 8.0+ or MariaDB 10.3+ (for manual setup)
- Git

## ⚙️ Configuration

Create a `.env` file (see `.env.example` for the full list). Example values for local Docker Compose usage:

```env
# Database Configuration
DB_URL=jdbc:mysql://db:3306/ftb_db
DB_USERNAME=ftb_user
DB_PASSWORD=ftb_password
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=ftb_db
MYSQL_USER=ftb_user
MYSQL_PASSWORD=ftb_password

# Application Configuration
APP_PORT=8080

# JWT Configuration
JWT_SECRET=change-me-to-long-random-string
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800
```

> ℹ️ When running the application outside of Docker Compose, adjust `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to point to your database host.

## 🚀 Quick Start

### Quick Start with Docker

1. Copy environment variables and adjust as needed:

   ```bash
   cp .env.example .env
   ```

   _.env is loaded into both containers via `env_file`, so keep it next to `docker-compose.yml` and fill all variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MYSQL__`, `JWT__`). If a value is missing, the app exits with a clear error — misconfiguration is immediately visible._

2. Build and launch the stack (Spring Boot + MySQL):

   ```bash
   docker compose up --build
   ```

   Optionally create `database/mysql_data` before the first run (Compose will create it automatically). The initial launch imports `database/ftb.sql` into MySQL; the app starts once the database healthcheck passes.

   _If you change `MYSQL_USER`/`MYSQL_PASSWORD`, remove `database/mysql_data` to re-initialise MySQL with the new credentials._

3. After startup the API is available at `http://localhost:8080`, and the database at `localhost:3306` (credentials from `.env`, e.g. `root`/`root`).

   Stop the stack with `docker compose down`; delete `database/mysql_data` to reset the database.

### Manual setup

1. **Prepare database**

   - Install MySQL/MariaDB and create database `ftb_db`, or reuse an existing instance.

2. **Build application**

   ```bash
   git clone <repository-url>
   cd mentorpiece_ftb
   cp .env.example .env
   # edit .env to point to your database
   ./mvnw clean package -DskipTests
   ```

3. **Run application without Docker**

   ```bash
   java -jar target/flightticketbooking-1.0.0.jar
   ```

   > Ensure all required environment variables (see `.env.example`) are exported in your shell — e.g. via manual `export`, `direnv`, or your preferred secrets manager.

4. **Access services**
   - Web UI: http://localhost:8080
   - API docs: http://localhost:8080/swagger-ui/index.html
   - OpenAPI spec: http://localhost:8080/v3/api-docs

## 🔐 Authentication

### Web Interface

- Login at `/login` with username/password
- Session-based authentication
- Role switching available in UI

### API Access

- **Basic Auth:** Use username/password for API calls
- **JWT:** Obtain token via `/api/auth/login` endpoint
- **Bearer Token:** Include in Authorization header

### Example API Usage

```bash
# Login and get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Use JWT token for API calls
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8080/api/flights
```

## 📁 Project Structure

```
src/main/java/com/aerotravel/flightticketbooking/
├── config/          # Security and application configuration
├── controller/      # Web controllers
├── rest/           # REST API controllers
├── model/          # JPA entities and DTOs
├── repository/     # Data access layer
├── services/       # Business logic
└── security/       # JWT and security components

src/main/resources/
├── templates/      # Thymeleaf templates
├── static/         # CSS, JS, images
└── application.properties
```

## 🔧 Development

### Running in Development Mode

```bash
export DB_PASSWORD="your_password"
./mvnw spring-boot:run
```

### Building for Production

```bash
./mvnw clean package -DskipTests
```

### Running Tests

```bash
./mvnw test
```

## 📊 API Documentation

- **Swagger UI:** `/swagger-ui/index.html`
- **OpenAPI JSON:** `/v3/api-docs`

## 🐳 Docker Deployment

- Multi-stage `Dockerfile` builds the application using Maven wrapper and runs it on Eclipse Temurin JRE 21.
- `docker-compose.yml` orchestrates MySQL and the Spring Boot service, mounts `database/mysql_data`, and loads `database/ftb.sql` on first start.
- Environment variables are injected through `.env`; adjust the file for different environments.

## 🔒 Security Notes

- Store strong secrets in `.env` or your secret manager.
- Use HTTPS in production deployments.
- Rotate JWT secrets periodically.

## 📝 TODO

- [ ] Enhanced flight search filters
- [ ] Email notification system
- [ ] Payment integration
- [ ] Mobile responsive improvements
- [ ] Performance monitoring
- [x] Docker compose setup
- [ ] CI/CD pipeline

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Original FlightBookingSystem project authors
- Spring Boot and Spring Security teams
- OpenAPI/Swagger community
