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
- MySQL 8.0+ or MariaDB 10.3+
- Git

## ⚙️ Configuration

### Environment Variables

Create a `.env` file (see `.env.example` for template):

```bash
# Database Configuration
export DB_URL=jdbc:mysql://localhost:3306/ftb_db
export DB_USERNAME=root
export DB_PASSWORD=your_secure_password

# Application Configuration
export APP_PORT=8080

# JWT Configuration (for API authentication)
export JWT_SECRET=your_super_secure_jwt_secret_key_here
export JWT_EXPIRATION=86400
export JWT_REFRESH_EXPIRATION=604800
```

## 🚀 Quick Start

### 1. Database Setup

**Option A: Docker (Recommended)**
```bash
docker run -p 3306:3306 --name ftb-mysql \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=ftb_db \
  -d mysql:8.0
```

**Option B: Local Installation**
- Install MySQL/MariaDB
- Create database: `CREATE DATABASE ftb_db;`

### 2. Application Setup

```bash
# Clone repository
git clone <repository-url>
cd mentorpiece_ftb

# Configure environment
cp .env.example .env
# Edit .env with your database credentials

# Build application
./mvnw clean package -DskipTests

# Run application
source .env
java -jar target/flightticketbooking-1.0.0.jar
```

### 3. Access Application

- **Web Interface:** http://localhost:8080
- **API Documentation:** http://localhost:8080/swagger-ui/index.html
- **API Specification:** http://localhost:8080/v3/api-docs

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

Access comprehensive API documentation at:
- **Swagger UI:** `/swagger-ui/index.html`
- **OpenAPI JSON:** `/v3/api-docs`

## 🐳 Docker Deployment

```dockerfile
FROM openjdk:21-jre-slim
COPY target/flightticketbooking-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔒 Security Notes

- Database credentials use environment variables
- JWT secrets should be cryptographically strong
- HTTPS recommended for production
- Regular security updates advised

## 📝 TODO

- [ ] Enhanced flight search filters
- [ ] Email notification system
- [ ] Payment integration
- [ ] Mobile responsive improvements
- [ ] Performance monitoring
- [ ] Docker compose setup
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