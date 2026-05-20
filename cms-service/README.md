# CMS Service

> **Content Management System Microservice** - Quản lý Người dùng, Quyền hạn, Nhóm và Tham số

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Development Guidelines](#development-guidelines)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

CMS Service là một microservice quản trị hệ thống, cung cấp các chức năng:

- **User Management**: Quản lý người dùng, phân quyền, khóa/mở khóa tài khoản
- **Authority Management**: Quản lý các quyền hạn trong hệ thống
- **Group Management**: Quản lý nhóm người dùng và gán quyền hạn
- **Parameter Management**: Quản lý tham số cấu hình hệ thống
- **Authentication**: Xác thực người dùng bằng JWT

Dịch vụ được xây dựng theo kiến trúc **Clean Architecture**, **CQRS** (Command Query Responsibility Segregation), và **DDD** (Domain-Driven Design).

---

## Features

### User Management
- ✅ Create, Read, Update, Delete users
- ✅ Lock/Unlock user accounts
- ✅ Search users by username or fullname
- ✅ Filter users by type (Admin/Customer)
- ✅ Change password functionality
- ✅ User registration

### Authority Management
- ✅ Create, Read, Update, Delete authorities
- ✅ Hierarchical authority structure with `fid` (parent ID)
- ✅ Custom `authKey` for permission mapping

### Group Management
- ✅ Create, Read, Update, Delete groups
- ✅ Assign users to groups
- ✅ Assign authorities to groups

### Parameter Management
- ✅ Create, Read, Update, Delete parameters
- ✅ System-wide configuration management

### Security
- ✅ JWT-based authentication
- ✅ Password encryption with BCrypt
- ✅ Role-based access control
- ✅ Redis integration for token caching

---

## Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.5.5 |
| **ORM** | Hibernate / Spring Data JPA | 7.1.1 / 3.2.0 |
| **Database** | MySQL | 8.0+ |
| **Cache** | Redis | 7.0+ |
| **Security** | Spring Security + JWT | - |
| **API Documentation** | SpringDoc OpenAPI | 2.7.0 |
| **Build Tool** | Maven | 3.9+ |
| **Libraries** | Lombok, MapStruct | 1.18.34 / 1.6.3 |

---

## Project Structure

```
cms-service/
├── src/
│   ├── main/
│   │   ├── java/com/cms/
│   │   │   ├── application/
│   │   │   │   ├── command/          # CQRS Commands (write operations)
│   │   │   │   ├── query/            # CQRS Queries (read operations)
│   │   │   │   └── dto/              # Request/Response DTOs
│   │   │   ├── domain/
│   │   │   │   ├── model/            # JPA Entities (Domain Models)
│   │   │   │   ├── repository/       # Repository Interfaces
│   │   │   │   └── common/           # Common exceptions
│   │   │   ├── infrastructure/
│   │   │   │   └── config/           # Spring configurations
│   │   │   └── interfaces/
│   │   │       └── rest/             # REST Controllers
│   │   └── resources/
│   │       ├── application.yml       # Main configuration
│   │       ├── application-dev.yml   # Development profile
│   │       └── application-prod.yml  # Production profile
│   └── test/                         # Unit & Integration tests
└── pom.xml                           # Maven dependencies
```

### Architecture Layers

| Layer | Responsibility | Example |
|-------|---------------|---------|
| **interfaces/rest** | HTTP request/response handling | `UserController` |
| **application** | Business logic orchestration | `CreateUserCommand` |
| **domain** | Core business logic & data models | `User`, `UserRepository` |
| **infrastructure** | External concerns (security, config) | `SecurityConfig` |

---

## Getting Started

### Prerequisites

- **JDK 21** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Redis 7.0+** - [Download](https://redis.io/download)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd microservice/cms-service
   ```

2. **Configure MySQL database**

   Create a database named `cms_service`:
   ```sql
   CREATE DATABASE cms_service;
   ```

   Update database credentials in `application-dev.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/cms_service
       username: your_username
       password: your_password
   ```

3. **Start Redis**

   ```bash
   # Windows
   redis-server

   # Linux/Mac
   sudo systemctl start redis
   ```

4. **Build the project**

   ```bash
   mvn clean install
   ```

### Running the Application

**Option 1: Using Maven**
```bash
mvn spring-boot:run
```

**Option 2: Using JAR**
```bash
mvn clean package
java -jar target/cms-service-1.0.0.jar
```

**Option 3: Using Docker**
```bash
docker-compose up
```

The application will start on:
- **API**: http://localhost:8081/cms-service
- **Swagger UI**: http://localhost:8081/cms-service/swagger-ui.html
- **API Docs**: http://localhost:8081/cms-service/api-docs

---

## API Documentation

### Swagger UI

Access the interactive API documentation at:
```
http://localhost:8081/cms-service/swagger-ui.html
```

### Main Endpoints

| Module | Base Path | Description |
|--------|-----------|-------------|
| Auth | `/v1/auth` | Login, Register, Change Password |
| Users | `/v1/users` | User CRUD operations |
| Authorities | `/v1/authorities` | Authority CRUD operations |
| Groups | `/v1/groups` | Group CRUD operations |
| Parameters | `/v1/parameters` | Parameter CRUD operations |

### Authentication

All endpoints (except auth) require JWT token in header:
```
Authorization: Bearer <your-jwt-token>
```

**Example Login Request:**
```bash
curl -X POST http://localhost:8081/cms-service/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

**Example Response:**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "user": {
      "id": "...",
      "username": "admin",
      "fullname": "Administrator"
    }
  }
}
```

---

## Configuration

### Application Profiles

| Profile | Description | Config File |
|---------|-------------|-------------|
| `dev` | Development environment | `application-dev.yml` |
| `prod` | Production environment | `application-prod.yml` |

### Key Configuration Properties

```yaml
# Server
server:
  port: 8081
  servlet:
    context-path: /cms-service

# JWT
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000        # 24 hours
  refresh-expiration: 604800000 # 7 days

# Database (dev)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_service
    username: admin
    password: 123456aA@

# Redis
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Environment Variables

```bash
# Required for production
export JWT_SECRET=your-secret-key
export DB_URL=jdbc:mysql://your-db-host:3306/cms_service
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
```

---

## Development Guidelines

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok for boilerplate code
- Write Javadoc for public APIs

### CQRS Pattern

**Command (Write Operations):**
```java
@Component
@RequiredArgsConstructor
public class CreateUserCommand implements ICommand {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse execute(CreateUserRequest request, String currentUserId) {
        // Business logic here
    }
}
```

**Query (Read Operations):**
```java
@Component
@RequiredArgsConstructor
public class GetAllUsersQuery implements IQuery {
    private final UserRepository userRepository;

    public List<UserResponse> execute() {
        // Read logic here
    }
}
```

### Important Rules

1. **Constructor Injection**: Only inject static dependencies (Repository, Service, Helper...)
2. **Runtime Data**: Pass userId, request data via `execute()` method parameters
3. **Transactional**: Use `@Transactional` on Command.execute() methods
4. **No Business Logic in Controllers**: Controllers only call Command/Query.execute()

📖 See [.github/project-rules.md](../.github/project-rules.md) for complete guidelines.

---

## Testing

### Run Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UserControllerTest

# With coverage
mvn test jacoco:report
```

### Test Structure

```
src/test/java/com/cms/
├── unit/              # Unit tests
├── integration/       # Integration tests
└── controller/        # Controller tests
```

---

## Deployment

### Docker Deployment

```bash
# Build image
docker build -t cms-service:1.0.0 .

# Run container
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=your-secret \
  cms-service:1.0.0
```

### Docker Compose

```bash
# Start all services (MySQL, Redis, CMS)
docker-compose up -d

# View logs
docker-compose logs -f cms-service

# Stop services
docker-compose down
```

### Production Checklist

- [ ] Update `application-prod.yml` with production values
- [ ] Set secure JWT_SECRET (minimum 256 bits)
- [ ] Configure production database connection
- [ ] Enable SSL/HTTPS
- [ ] Set up Redis clustering for high availability
- [ ] Configure log aggregation
- [ ] Enable health check endpoints
- [ ] Set up monitoring and alerting

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Port 8081 already in use | Change `server.port` in application.yml |
| Database connection failed | Verify MySQL is running and credentials are correct |
| JWT token expired | Increase `jwt.expiration` or implement refresh token flow |
| Redis connection refused | Start Redis server with `redis-server` |

### Health Check

```bash
curl http://localhost:8081/cms-service/actuator/health
```

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Pull Request Guidelines

- Follow the project coding conventions
- Write unit tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-05-19 | Initial release with User, Authority, Group, Parameter management |

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contact & Support

- **Project Maintainer**: Hung Nguyen Van [0813338836]
- **Issue Tracker**: [GitHub Issues](../../issues)
- **Documentation**: [.github/project-rules.md](../.github/project-rules.md)

---

**Built with ❤️ using Spring Boot**
