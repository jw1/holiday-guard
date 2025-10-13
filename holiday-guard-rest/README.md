# Holiday Guard REST Module

This module provides the HTTP REST API and controller layer for Holiday Guard. It exposes schedule management, query, and calendar visualization endpoints.

## REST Controllers

### ShouldRunController
**Purpose:** Core API for querying whether schedules should run on specific dates.

**Base Path:** `/api/v1/schedules/{scheduleId}/should-run`

**Endpoints:**
```http
GET /api/v1/schedules/{scheduleId}/should-run?client={clientId}
POST /api/v1/schedules/{scheduleId}/should-run
```

**Security:** Requires `ROLE_USER` or `ROLE_ADMIN`

**Example Request (GET):**
```bash
curl "http://localhost:8080/api/v1/schedules/1/should-run?client=payroll-service"
```

**Example Request (POST):**
```bash
curl -X POST "http://localhost:8080/api/v1/schedules/1/should-run" \
  -H "Content-Type: application/json" \
  -d '{"queryDate": "2025-12-25", "clientIdentifier": "report-generator"}'
```

**Response:**
```json
{
  "scheduleId": 1,
  "queryDate": "2025-12-25",
  "shouldRun": false,
  "runStatus": "FORCE_SKIP",
  "reason": "Christmas Day",
  "deviationApplied": true,
  "versionId": 10
}
```

### ScheduleController
**Purpose:** CRUD operations for schedules (management only).

**Base Path:** `/api/v1/schedules`

**Conditional:** Only registered when `DataProvider.supportsManagement()` returns true (H2 profile).

**Security:**
- GET endpoints: `ROLE_USER` or `ROLE_ADMIN`
- POST/PUT/DELETE: `ROLE_ADMIN` only

**Endpoints:**
```http
GET    /api/v1/schedules                    # List all schedules
GET    /api/v1/schedules/{id}                # Get schedule by ID
GET    /api/v1/schedules/by-name/{name}     # Get schedule by name
POST   /api/v1/schedules                    # Create new schedule
PUT    /api/v1/schedules/{id}                # Update schedule
DELETE /api/v1/schedules/{id}                # Delete schedule
```

**Create Schedule Request:**
```json
{
  "name": "Payroll Schedule",
  "description": "US payroll processing calendar",
  "country": "US",
  "active": true
}
```

### ScheduleVersionController
**Purpose:** Manage schedule versions and rule updates (management only).

**Base Path:** `/api/v1/schedules/{scheduleId}/versions`

**Conditional:** Only registered for H2 profile.

**Security:** Requires `ROLE_ADMIN`

**Endpoints:**
```http
GET  /api/v1/schedules/{scheduleId}/versions         # List version history
GET  /api/v1/schedules/{scheduleId}/versions/active  # Get active version
POST /api/v1/schedules/{scheduleId}/versions         # Create new version with rule update
```

**Update Rule Request:**
```json
{
  "effectiveFrom": "2025-01-01T00:00:00Z",
  "rule": {
    "ruleType": "WEEKDAYS_ONLY",
    "ruleConfig": null,
    "effectiveFrom": "2025-01-01",
    "active": true
  },
  "deviations": [
    {
      "date": "2025-12-25",
      "action": "FORCE_SKIP",
      "reason": "Christmas Day",
      "createdBy": "admin",
      "expiresAt": null
    }
  ]
}
```

### CalendarViewController
**Purpose:** Multi-schedule calendar data for frontend calendar UI.

**Base Path:** `/api/v1/calendar-view`

**Security:** Requires `ROLE_USER` or `ROLE_ADMIN`

**Endpoints:**
```http
GET /api/v1/calendar-view?yearMonth=2025-10&scheduleIds=1,2,3
```

**Response Structure:**
```json
{
  "yearMonth": "2025-10",
  "schedules": [
    {
      "scheduleId": 1,
      "scheduleName": "Payroll Schedule",
      "yearMonth": "2025-10",
      "days": [
        {
          "date": "2025-10-01",
          "status": "RUN",
          "reason": "Scheduled to run"
        },
        {
          "date": "2025-10-04",
          "status": "SKIP",
          "reason": "Weekend"
        }
      ]
    }
  ]
}
```

**Optimization:** Normalized structure reduces JSON payload by ~70% compared to flat structure.

### DashboardController
**Purpose:** Today's status summary for all active schedules.

**Base Path:** `/api/v1/dashboard`

**Security:** Requires `ROLE_USER` or `ROLE_ADMIN`

**Endpoints:**
```http
GET /api/v1/dashboard/schedule-status
```

**Response:**
```json
[
  {
    "scheduleId": 1,
    "scheduleName": "Payroll Schedule",
    "status": "RUN",
    "shouldRun": true,
    "reason": "Scheduled to run"
  },
  {
    "scheduleId": 2,
    "scheduleName": "Weekend Backup",
    "status": "SKIP",
    "shouldRun": false,
    "reason": "Weekday only schedule"
  }
}
```

### AuditLogController
**Purpose:** Query audit trail of shouldRun queries (H2 profile only).

**Base Path:** `/api/v1/audit-logs`

**Conditional:** Only registered for H2 profile.

**Security:** Requires `ROLE_ADMIN`

**Endpoints:**
```http
GET /api/v1/audit-logs?scheduleId=1&limit=100
GET /api/v1/audit-logs/recent?limit=50
```

### UserController
**Purpose:** User authentication and profile information.

**Base Path:** `/api/v1/user`

**Security:** Requires authentication

**Endpoints:**
```http
GET /api/v1/user/current  # Get current authenticated user info
```

## DTOs (Data Transfer Objects)

### Request DTOs
Located in `com.jw.holidayguard.dto.request`:

- **CreateScheduleRequest** - Schedule creation
- **UpdateScheduleRequest** - Schedule updates
- **CreateRuleRequest** - Rule definition
- **UpdateRuleRequest** - Rule and deviation updates
- **CreateDeviationRequest** - Deviation definition
- **ShouldRunQueryRequest** - shouldRun query parameters

### Response DTOs
Located in `com.jw.holidayguard.dto.response`:

- **ShouldRunQueryResponse** - shouldRun query results
- **ScheduleResponse** - Schedule details
- **VersionResponse** - Version history
- **MultiScheduleCalendarView** - Calendar visualization data
- **ScheduleDashboardView** - Dashboard summary

## Security Configuration

Holiday Guard uses Spring Security with in-memory authentication (development) or external auth providers (production).

### Roles
- **ROLE_USER** - Read-only access to schedules and queries
- **ROLE_ADMIN** - Full CRUD access including management operations

### Endpoint Security Matrix

| Endpoint | USER | ADMIN | Public |
|----------|------|-------|--------|
| GET /should-run | ✅ | ✅ | ❌ |
| POST /should-run | ✅ | ✅ | ❌ |
| GET /schedules | ✅ | ✅ | ❌ |
| POST /schedules | ❌ | ✅ | ❌ |
| PUT /schedules | ❌ | ✅ | ❌ |
| DELETE /schedules | ❌ | ✅ | ❌ |
| GET /calendar-view | ✅ | ✅ | ❌ |
| GET /dashboard | ✅ | ✅ | ❌ |
| GET /audit-logs | ❌ | ✅ | ❌ |

### CSRF Protection
- Enabled for state-changing operations (POST, PUT, DELETE)
- Disabled for GET requests
- Token must be included in request headers

### CORS Configuration
- Configurable for frontend integration
- Allows specified origins and methods
- Credentials support for authenticated requests

## Exception Handling

### GlobalExceptionHandler
Centralized exception handling using `@ControllerAdvice`.

**Mapped Exceptions:**

| Exception | HTTP Status | Error Code |
|-----------|-------------|------------|
| ScheduleNotFoundException | 404 Not Found | SCHEDULE_NOT_FOUND |
| DuplicateScheduleException | 409 Conflict | DUPLICATE_SCHEDULE |
| IllegalArgumentException | 400 Bad Request | INVALID_REQUEST |
| ValidationException | 400 Bad Request | VALIDATION_ERROR |
| AccessDeniedException | 403 Forbidden | ACCESS_DENIED |
| RuntimeException | 500 Internal Server Error | INTERNAL_ERROR |

**Error Response Format:**
```json
{
  "error": "SCHEDULE_NOT_FOUND",
  "message": "Schedule with ID 999 not found",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules/999"
}
```

## Validation

Request DTOs use Bean Validation annotations:

```java
public record CreateScheduleRequest(
    @NotBlank(message = "Schedule name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description
) {}
```

Validation errors return 400 Bad Request with detailed field-level messages.

## API Versioning

Current version: **v1**

Base path includes version: `/api/v1/...`

Future versions will use separate paths: `/api/v2/...`

## Content Negotiation

- **Request:** `Content-Type: application/json`
- **Response:** `Content-Type: application/json`
- All dates use ISO 8601 format: `YYYY-MM-DD`
- Timestamps use ISO 8601 with timezone: `YYYY-MM-DDTHH:MM:SSZ`

## Rate Limiting

*Not currently implemented - future enhancement*

Planned features:
- Per-user rate limits
- Per-schedule rate limits for shouldRun queries
- HTTP 429 Too Many Requests response

## Testing

### Controller Tests
```java
@WebMvcTest(controllers = ShouldRunController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
class ShouldRunControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleQueryService service;

    @Test
    void shouldRunTodayReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/schedules/1/should-run")
                .with(user("user").roles("USER")))
            .andExpect(status().isOk());
    }
}
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ScheduleControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createScheduleSucceeds() {
        ResponseEntity<ScheduleResponse> response = restTemplate
            .withBasicAuth("admin", "admin")
            .postForEntity("/api/v1/schedules", request, ScheduleResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

## OpenAPI / Swagger Documentation

*Future enhancement - not currently implemented*

Planned integration with SpringDoc OpenAPI for interactive API documentation.

## Dependencies

- **holiday-guard-core** - Business logic services
- **Spring Boot Web** - REST framework
- **Spring Security** - Authentication and authorization
- **Jackson** - JSON serialization
- **Bean Validation** - Request validation

## Performance Considerations

- **Response Caching:** Calendar views can be cached with short TTL
- **Pagination:** List endpoints support limit parameters
- **Normalized DTOs:** Calendar views use hierarchical structure to reduce payload size
- **Lazy Loading:** Related entities loaded on-demand

## Best Practices

1. **Idempotency:** GET requests are idempotent and cacheable
2. **Resource URLs:** RESTful resource-based URLs
3. **HTTP Status Codes:** Semantic status codes for all responses
4. **Error Messages:** User-friendly error messages with technical details
5. **API Versioning:** Version prefix in URL for backward compatibility
