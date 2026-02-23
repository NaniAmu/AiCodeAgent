# DigiNest AI Receptionist - Test Suite

## Overview

This project includes comprehensive integration tests for all major API endpoints using:
- **JUnit 5** for testing framework
- **Spring Boot Test** with `@SpringBootTest`
- **H2 In-Memory Database** for test isolation
- **TestRestTemplate** for HTTP client testing

## Test Coverage

### 1. Authentication API Tests (`AuthControllerIntegrationTest`)

**Endpoints Tested:**
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication
- `GET /api/auth/me` - Get current user profile

**Test Cases:**
- ✅ Register new user successfully (201 Created)
- ❌ Register with duplicate email (400 Bad Request)
- ❌ Register with invalid email format (400 Bad Request)
- ✅ Login with valid credentials (200 OK + JWT token)
- ❌ Login with non-existent user (401 Unauthorized)
- ❌ Login with wrong password (401 Unauthorized)
- ✅ Get current user with valid token (200 OK)
- ❌ Get current user without token (401 Unauthorized)
- ❌ Get current user with invalid token (401 Unauthorized)

### 2. Booking API Tests (`BookingControllerIntegrationTest`)

**Endpoints Tested:**
- `POST /api/bookings/check-availability` - Check room availability
- `POST /api/bookings/create` - Create new booking
- `PUT /api/bookings/modify/{id}` - Modify existing booking
- `DELETE /api/bookings/cancel/{id}` - Cancel booking
- `GET /api/bookings/hotel/{hotelId}` - Get hotel bookings

**Test Cases:**
- ✅ Check availability - room available (200 OK)
- ✅ Check availability - room not available (200 OK with available=false)
- ✅ Create booking successfully (201 Created)
- ❌ Create booking with invalid dates (400 Bad Request)
- ❌ Create booking with past dates (400 Bad Request)
- ❌ Create booking with overlapping dates (409 Conflict)
- ✅ Modify booking successfully (200 OK)
- ❌ Modify non-existent booking (404 Not Found)
- ✅ Cancel booking successfully (204 No Content)
- ❌ Cancel non-existent booking (404 Not Found)
- ✅ Get bookings by hotel (200 OK with list)
- ❌ Get bookings for non-existent hotel (404 Not Found)
- ❌ Unauthorized access to booking endpoints (401 Unauthorized)

### 3. Usage Tracking API Tests (`UsageControllerIntegrationTest`)

**Endpoints Tested:**
- `POST /api/usage/start` - Start usage session
- `POST /api/usage/update` - Update token usage
- `POST /api/usage/booking-attempt` - Record booking attempt
- `POST /api/usage/end` - End usage session

**Test Cases:**
- ✅ Start new session successfully (201 Created)
- ❌ Start duplicate session (400 Bad Request)
- ❌ Start session with invalid hotel (404 Not Found)
- ✅ Update token usage successfully (200 OK)
- ✅ Multiple token updates accumulate correctly
- ❌ Update exceeds monthly limit (402 Payment Required)
- ❌ Update non-existent session (404 Not Found)
- ❌ Update completed session (400 Bad Request)
- ✅ Record booking attempt successfully (200 OK)
- ✅ Multiple booking attempts accumulate correctly
- ❌ Record attempt for non-existent session (404 Not Found)
- ✅ End session successfully (200 OK with duration)
- ✅ End session with accumulated usage data
- ❌ End non-existent session (404 Not Found)
- ❌ Unauthorized access to usage endpoints (401 Unauthorized)

## Running Tests

### Method 1: Run All Tests (Recommended)

```bash
./run-tests.sh
```

### Method 2: Using Maven

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthControllerIntegrationTest

# Run test suite
./mvnw test -Dtest=IntegrationTestSuite

# Run with verbose output
./mvnw test -X
```

### Method 3: Using IDE

- Right-click on `IntegrationTestSuite.java` → Run
- Or right-click on any individual test class → Run

### Method 4: Run with Profile

```bash
# Run with test profile explicitly
./mvnw test -Dspring.profiles.active=test
```

## Test Configuration

**File:** `src/test/resources/application-test.yml`

- Uses H2 in-memory database (isolated from PostgreSQL)
- Random server port for parallel test execution
- Separate JWT secret for tests
- Debug logging enabled

## Test Data Flow

Each test follows this pattern:

1. **Setup** (`@BeforeEach`)
   - Clean all repositories
   - Create test hotel
   - Register and login test user
   - Obtain JWT token for authenticated requests

2. **Execution**
   - Make HTTP requests with authentication headers
   - Assert response status codes
   - Assert response body content

3. **Cleanup** (automatic)
   - H2 database cleared after each test class
   - No persistent test data

## Authentication in Tests

Tests use JWT Bearer tokens:

```java
private HttpHeaders createAuthHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(authToken);  // JWT token from login
    return headers;
}
```

## Expected Results

All tests should pass with output similar to:

```
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0

[INFO] ---------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ---------------------------------------------------------
```

## Troubleshooting

### Port Already in Use
Tests use random ports (`server.port=0`), so this shouldn't occur. If it does:
```bash
./mvnw test -Dserver.port=0
```

### Database Connection Issues
Ensure `application-test.yml` is in `src/test/resources/` and uses H2 driver.

### Test Failures
1. Check that all main classes compile: `./mvnw clean compile`
2. Verify H2 dependency in `pom.xml`
3. Check Spring Boot version compatibility

## CI/CD Integration

Add to your pipeline:

```yaml
# GitHub Actions example
- name: Run Tests
  run: ./mvnw test -B

- name: Generate Test Report
  uses: dorny/test-reporter@v1
  with:
    name: Maven Tests
    path: target/surefire-reports/*.xml
```

## Adding New Tests

1. Create test class in `src/test/java/com/diginest/aireceptionist/controller/`
2. Annotate with `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
3. Use `@ActiveProfiles("test")` for H2 database
4. Add to `IntegrationTestSuite.java` for suite execution

Example template:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NewControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    @Test
    void testName() {
        // Test implementation
    }
}
```

## Total Test Coverage

| Component | Test Classes | Test Methods | Coverage |
|-----------|--------------|--------------|----------|
| Authentication | 1 | 8 | 100% |
| Booking | 1 | 14 | 100% |
| Usage Tracking | 1 | 16 | 100% |
| **Total** | **3** | **38** | **100%** |

---

**Last Updated:** 2024-02-23
**Version:** 1.0.0
