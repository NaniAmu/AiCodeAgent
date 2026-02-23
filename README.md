# DigiNest AI Receptionist V1

Production-level AI Voice SaaS for Hotels - Base Spring Boot Project Structure.

## Stack

- **Spring Boot 3.2.2** (Java 21)
- **PostgreSQL**
- **JWT Authentication**
- **Spring Security**
- **Docker**

## Project Structure

```
├── src/main/java/com/diginest/aireceptionist/
│   ├── AiReceptionistApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   ├── JwtResponse.java
│   │   └── LoginRequest.java
│   ├── entity/
│   │   ├── BaseEntity.java        # Multi-tenant base (hotel_id)
│   │   ├── Hotel.java
│   │   └── User.java
│   ├── repository/
│   │   ├── HotelRepository.java
│   │   └── UserRepository.java
│   ├── security/
│   │   ├── CustomUserDetailsService.java
│   │   └── jwt/
│   │       ├── JwtAuthenticationFilter.java
│   │       └── JwtTokenProvider.java
│   └── service/
│       └── AuthService.java
├── src/main/resources/
│   └── application.yml
├── Dockerfile
└── pom.xml
```

## Quick Start (Local Development)

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 15+

### 1. Setup Database

```bash
# Create database
psql -U postgres -c "CREATE DATABASE diginest;"
```

### 2. Configure Environment

Create a `.env` file or set environment variables:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters-long
```

### 3. Run Application

```bash
# Build and run
./mvnw spring-boot:run

# Or build JAR first
./mvnw clean package
java -jar target/ai-receptionist-1.0.0.jar
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login with email/password |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check (public) |

## Docker Deployment

### Build and Run with Docker

```bash
# Build image
docker build -t diginest-ai-receptionist .

# Run with environment variables
docker run -p 8080:8080 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=your-secret-key \
  diginest-ai-receptionist
```

### Docker Compose (Recommended)

Create a `docker-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: diginest
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      JWT_SECRET: your-secret-key-here-must-be-32-chars-min
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/diginest
    depends_on:
      - postgres

volumes:
  postgres_data:
```

Run:
```bash
docker-compose up -d
```

## Multi-Tenant Architecture

All tenant-specific entities extend `BaseEntity` which includes:
- `hotel_id` - Foreign key to the Hotel entity
- `created_at` / `updated_at` - Automatic timestamps

This ensures data isolation between hotels.

## Security

- **JWT Tokens**: Stateless authentication with configurable expiration (default: 24 hours)
- **BCrypt**: Password hashing
- **Role-based access**: SUPER_ADMIN, HOTEL_ADMIN, HOTEL_STAFF
- **CORS**: Configurable in SecurityConfig

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | postgres | Database username |
| `DB_PASSWORD` | postgres | Database password |
| `JWT_SECRET` | (embedded) | JWT signing secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | 86400000 | Token expiration (24 hours) |

## Next Steps

1. Implement user registration endpoint
2. Add hotel management endpoints
3. Implement booking logic
4. Add usage tracking
5. Integrate AI voice capabilities

## License

Proprietary - DigiNest AI Receptionist V1
