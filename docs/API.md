# Holiday Guard REST API Reference

Complete reference for the Holiday Guard REST API v1.

**Base URL:** `http://localhost:8080/api/v1`

**Authentication:** HTTP Basic Auth (development) or Bearer Token (production)

**Content Type:** `application/json`

---

## Table of Contents

- [Authentication](#authentication)
- [Schedule Query API](#schedule-query-api)
- [Schedule Management API](#schedule-management-api)
- [Version Management API](#version-management-api)
- [Calendar View API](#calendar-view-api)
- [Dashboard API](#dashboard-api)
- [Audit Log API](#audit-log-api)
- [Error Responses](#error-responses)

---

## Authentication

All API endpoints require authentication using HTTP Basic Auth or Bearer tokens.

### Default Credentials (Development)
- **Admin:** `admin` / `admin`
- **User:** `user` / `user`

### Headers
```http
Authorization: Basic dXNlcjp1c2Vy
Content-Type: application/json
```

### Roles
- **ROLE_USER** - Read-only access to schedules and queries
- **ROLE_ADMIN** - Full CRUD access including management operations

---

## Schedule Query API

### Query if Schedule Should Run Today

**Endpoint:** `GET /schedules/{scheduleId}/should-run`

**Description:** Query if a schedule should run today or on a specific date.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Path Parameters:**
- `scheduleId` (required) - Schedule ID

**Query Parameters:**
- `client` (optional) - Client identifier for audit logging

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/schedules/1/should-run?client=payroll-service" \
  -u user:user
```

**Example Response (200 OK):**
```json
{
  "scheduleId": 1,
  "queryDate": "2025-10-13",
  "shouldRun": true,
  "runStatus": "RUN",
  "reason": "Scheduled to run - weekday",
  "deviationApplied": false,
  "versionId": 10
}
```

### Query for Specific Date (POST)

**Endpoint:** `POST /schedules/{scheduleId}/should-run`

**Description:** Query if a schedule should run on a specific date.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Path Parameters:**
- `scheduleId` (required) - Schedule ID

**Request Body:**
```json
{
  "queryDate": "2025-12-25",
  "clientIdentifier": "report-generator"
}
```

**Fields:**
- `queryDate` (optional) - ISO date (YYYY-MM-DD). Defaults to today if omitted.
- `clientIdentifier` (optional) - Client identifier for audit logging

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/schedules/1/should-run" \
  -u user:user \
  -H "Content-Type: application/json" \
  -d '{
    "queryDate": "2025-12-25",
    "clientIdentifier": "report-generator"
  }'
```

**Example Response (200 OK):**
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

**RunStatus Values:**
- `RUN` - Schedule should run (rule matches)
- `SKIP` - Schedule should not run (rule doesn't match)
- `FORCE_RUN` - Deviation forces execution (overrides rule)
- `FORCE_SKIP` - Deviation prevents execution (overrides rule)

---

## Schedule Management API

*Note: These endpoints are only available when using the H2 profile (management enabled).*

### List All Schedules

**Endpoint:** `GET /schedules`

**Description:** Retrieve all schedules in the system.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/schedules" \
  -u user:user
```

**Example Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Payroll Schedule",
    "description": "US payroll processing calendar",
    "country": "US",
    "active": true,
    "createdAt": "2025-01-01T00:00:00Z",
    "updatedAt": "2025-01-01T00:00:00Z"
  },
  {
    "id": 2,
    "name": "ACH Processing",
    "description": "Federal Reserve business days",
    "country": "US",
    "active": true,
    "createdAt": "2025-01-01T00:00:00Z",
    "updatedAt": "2025-01-01T00:00:00Z"
  }
]
```

### Get Schedule by ID

**Endpoint:** `GET /schedules/{id}`

**Description:** Retrieve a specific schedule by ID.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Path Parameters:**
- `id` (required) - Schedule ID

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/schedules/1" \
  -u user:user
```

**Example Response (200 OK):**
```json
{
  "id": 1,
  "name": "Payroll Schedule",
  "description": "US payroll processing calendar",
  "country": "US",
  "active": true,
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-01-01T00:00:00Z"
}
```

### Get Schedule by Name

**Endpoint:** `GET /schedules/by-name/{name}`

**Description:** Retrieve a schedule by its unique name.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Path Parameters:**
- `name` (required) - Schedule name (URL encoded)

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/schedules/by-name/Payroll%20Schedule" \
  -u user:user
```

### Create Schedule

**Endpoint:** `POST /schedules`

**Description:** Create a new schedule.

**Authorization:** `ROLE_ADMIN`

**Request Body:**
```json
{
  "name": "New Schedule",
  "description": "Schedule description",
  "country": "US",
  "active": true
}
```

**Fields:**
- `name` (required) - Unique schedule name (max 100 characters)
- `description` (optional) - Description (max 500 characters)
- `country` (optional) - Country code (default: "US")
- `active` (optional) - Active status (default: true)

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/schedules" \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Schedule",
    "description": "My new schedule",
    "country": "US",
    "active": true
  }'
```

**Example Response (201 Created):**
```json
{
  "id": 3,
  "name": "New Schedule",
  "description": "My new schedule",
  "country": "US",
  "active": true,
  "createdAt": "2025-10-13T17:30:00Z",
  "updatedAt": "2025-10-13T17:30:00Z"
}
```

### Update Schedule

**Endpoint:** `PUT /schedules/{id}`

**Description:** Update an existing schedule.

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**
- `id` (required) - Schedule ID

**Request Body:**
```json
{
  "name": "Updated Schedule Name",
  "description": "Updated description",
  "country": "US",
  "active": false
}
```

**Example Request:**
```bash
curl -X PUT "http://localhost:8080/api/v1/schedules/1" \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Schedule Name",
    "description": "Updated description",
    "country": "US",
    "active": false
  }'
```

**Example Response (200 OK):**
```json
{
  "id": 1,
  "name": "Updated Schedule Name",
  "description": "Updated description",
  "country": "US",
  "active": false,
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-10-13T17:30:00Z"
}
```

### Delete Schedule

**Endpoint:** `DELETE /schedules/{id}`

**Description:** Delete a schedule (soft delete - sets active to false).

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**
- `id` (required) - Schedule ID

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/schedules/1" \
  -u admin:admin
```

**Example Response (204 No Content)**

---

## Version Management API

*Note: Only available with H2 profile.*

### List Version History

**Endpoint:** `GET /schedules/{scheduleId}/versions`

**Description:** Get complete version history for a schedule.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Path Parameters:**
- `scheduleId` (required) - Schedule ID

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/schedules/1/versions" \
  -u user:user
```

**Example Response (200 OK):**
```json
[
  {
    "id": 10,
    "scheduleId": 1,
    "versionNumber": 2,
    "active": true,
    "effectiveFrom": "2025-10-01T00:00:00Z",
    "createdAt": "2025-10-01T00:00:00Z",
    "createdBy": "admin"
  },
  {
    "id": 5,
    "scheduleId": 1,
    "versionNumber": 1,
    "active": false,
    "effectiveFrom": "2025-01-01T00:00:00Z",
    "createdAt": "2025-01-01T00:00:00Z",
    "createdBy": "system"
  }
]
```

### Get Active Version

**Endpoint:** `GET /schedules/{scheduleId}/versions/active`

**Description:** Get the currently active version for a schedule.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Path Parameters:**
- `scheduleId` (required) - Schedule ID

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/schedules/1/versions/active" \
  -u user:user
```

**Example Response (200 OK):**
```json
{
  "id": 10,
  "scheduleId": 1,
  "versionNumber": 2,
  "active": true,
  "effectiveFrom": "2025-10-01T00:00:00Z",
  "createdAt": "2025-10-01T00:00:00Z",
  "createdBy": "admin",
  "rule": {
    "id": 20,
    "ruleType": "WEEKDAYS_ONLY",
    "ruleConfig": null,
    "effectiveFrom": "2025-10-01T00:00:00Z"
  },
  "deviations": [
    {
      "id": 30,
      "deviationDate": "2025-12-25",
      "action": "FORCE_SKIP",
      "reason": "Christmas Day",
      "createdBy": "admin",
      "expiresAt": null
    }
  ]
}
```

### Create New Version (Update Rule)

**Endpoint:** `POST /schedules/{scheduleId}/versions`

**Description:** Create a new version with updated rule and deviations.

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**
- `scheduleId` (required) - Schedule ID

**Request Body:**
```json
{
  "effectiveFrom": "2025-11-01T00:00:00Z",
  "rule": {
    "ruleType": "WEEKDAYS_ONLY",
    "ruleConfig": null,
    "effectiveFrom": "2025-11-01",
    "active": true
  },
  "deviations": [
    {
      "date": "2025-11-28",
      "action": "FORCE_SKIP",
      "reason": "Thanksgiving Day",
      "createdBy": "admin",
      "expiresAt": null
    },
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

**Rule Types:**
- `WEEKDAYS_ONLY` - Monday through Friday (no config)
- `ALL_DAYS` - Every day (no config)
- `NO_DAYS` - Never runs (no config)
- `CRON_EXPRESSION` - 6-field cron (config: cron expression)
- `US_FEDERAL_RESERVE_BUSINESS_DAYS` - US banking days (no config)
- `SPECIFIC_DATES` - Specific dates only (config: comma-separated ISO dates)

**Deviation Actions:**
- `FORCE_RUN` - Override rule to force execution
- `FORCE_SKIP` - Override rule to prevent execution

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/schedules/1/versions" \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d @version-update.json
```

**Example Response (201 Created):**
```json
{
  "id": 11,
  "scheduleId": 1,
  "versionNumber": 3,
  "active": true,
  "effectiveFrom": "2025-11-01T00:00:00Z",
  "createdAt": "2025-10-13T17:30:00Z",
  "createdBy": "admin"
}
```

---

## Calendar View API

### Get Multi-Schedule Calendar View

**Endpoint:** `GET /calendar-view`

**Description:** Get calendar data for multiple schedules for a specific month.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Query Parameters:**
- `yearMonth` (required) - Month in YYYY-MM format
- `scheduleIds` (required) - Comma-separated schedule IDs

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/calendar-view?yearMonth=2025-10&scheduleIds=1,2,3" \
  -u user:user
```

**Example Response (200 OK):**
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
        },
        {
          "date": "2025-10-13",
          "status": "FORCE_SKIP",
          "reason": "Columbus Day"
        }
      ]
    },
    {
      "scheduleId": 2,
      "scheduleName": "Weekend Backup",
      "yearMonth": "2025-10",
      "days": [
        {
          "date": "2025-10-04",
          "status": "RUN",
          "reason": "Weekend schedule"
        },
        {
          "date": "2025-10-05",
          "status": "RUN",
          "reason": "Weekend schedule"
        }
      ]
    }
  ]
}
```

**Response Structure:**
- Normalized hierarchy reduces payload size by ~70%
- Schedule metadata not repeated for each day
- Only dates where status changes are included

---

## Dashboard API

### Get Today's Schedule Status

**Endpoint:** `GET /dashboard/schedule-status`

**Description:** Get today's run status for all active schedules.

**Authorization:** `ROLE_USER` or `ROLE_ADMIN`

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/dashboard/schedule-status" \
  -u user:user
```

**Example Response (200 OK):**
```json
[
  {
    "scheduleId": 1,
    "scheduleName": "Payroll Schedule",
    "status": "RUN",
    "shouldRun": true,
    "reason": "Scheduled to run - weekday"
  },
  {
    "scheduleId": 2,
    "scheduleName": "Weekend Backup",
    "status": "SKIP",
    "shouldRun": false,
    "reason": "Weekday only schedule"
  },
  {
    "scheduleId": 3,
    "scheduleName": "Holiday Schedule",
    "status": "FORCE_SKIP",
    "shouldRun": false,
    "reason": "Federal holiday"
  }
]
```

---

## Audit Log API

*Note: Only available with H2 profile.*

### Query Audit Logs

**Endpoint:** `GET /audit-logs`

**Description:** Query audit trail of shouldRun queries.

**Authorization:** `ROLE_ADMIN`

**Query Parameters:**
- `scheduleId` (optional) - Filter by schedule ID
- `limit` (optional) - Maximum number of results (default: 100)

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/audit-logs?scheduleId=1&limit=50" \
  -u admin:admin
```

**Example Response (200 OK):**
```json
[
  {
    "id": 1001,
    "scheduleId": 1,
    "versionId": 10,
    "queryDate": "2025-10-13",
    "result": true,
    "clientIdentifier": "payroll-service",
    "queryTimestamp": "2025-10-13T08:30:00Z"
  },
  {
    "id": 1000,
    "scheduleId": 1,
    "versionId": 10,
    "queryDate": "2025-10-12",
    "result": false,
    "clientIdentifier": "payroll-service",
    "queryTimestamp": "2025-10-12T08:30:00Z"
  }
]
```

### Get Recent Audit Logs

**Endpoint:** `GET /audit-logs/recent`

**Description:** Get most recent audit log entries across all schedules.

**Authorization:** `ROLE_ADMIN`

**Query Parameters:**
- `limit` (optional) - Maximum number of results (default: 50)

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/v1/audit-logs/recent?limit=25" \
  -u admin:admin
```

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules/999"
}
```

### HTTP Status Codes

| Status | Description |
|--------|-------------|
| 200 OK | Successful request |
| 201 Created | Resource created successfully |
| 204 No Content | Successful deletion |
| 400 Bad Request | Invalid request parameters or body |
| 401 Unauthorized | Missing or invalid authentication |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Resource not found |
| 409 Conflict | Duplicate resource (e.g., schedule name) |
| 500 Internal Server Error | Server error |

### Error Codes

| Error Code | Description |
|------------|-------------|
| SCHEDULE_NOT_FOUND | Schedule with specified ID not found |
| DUPLICATE_SCHEDULE | Schedule with name already exists |
| INVALID_REQUEST | Invalid request parameters or body |
| VALIDATION_ERROR | Request validation failed |
| ACCESS_DENIED | Insufficient permissions |
| INTERNAL_ERROR | Internal server error |

### Example Error Responses

**404 Schedule Not Found:**
```json
{
  "error": "SCHEDULE_NOT_FOUND",
  "message": "Schedule with ID 999 not found",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules/999"
}
```

**409 Duplicate Schedule:**
```json
{
  "error": "DUPLICATE_SCHEDULE",
  "message": "Schedule with name 'Payroll Schedule' already exists",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules"
}
```

**400 Validation Error:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Schedule name is required",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules"
}
```

**401 Unauthorized:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication required",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules"
}
```

**403 Forbidden:**
```json
{
  "error": "ACCESS_DENIED",
  "message": "Insufficient permissions - requires ROLE_ADMIN",
  "timestamp": "2025-10-13T17:30:00Z",
  "path": "/api/v1/schedules"
}
```

---

## Integration Examples

### Shell Script Integration

```bash
#!/bin/bash
# Check if payroll should run and execute if true

SCHEDULE_ID=1
API_URL="http://localhost:8080/api/v1"
AUTH="user:user"

response=$(curl -s -u "$AUTH" "$API_URL/schedules/$SCHEDULE_ID/should-run?client=payroll-script")
should_run=$(echo "$response" | jq -r '.shouldRun')

if [ "$should_run" = "true" ]; then
    echo "Running payroll..."
    /opt/payroll/run-payroll.sh
else
    echo "Skipping payroll - not scheduled"
fi
```

### Python Integration

```python
import requests
from datetime import date

def should_schedule_run(schedule_id: int, query_date: date = None) -> bool:
    """Query if schedule should run on specific date."""
    url = f"http://localhost:8080/api/v1/schedules/{schedule_id}/should-run"
    auth = ("user", "user")

    if query_date:
        payload = {
            "queryDate": query_date.isoformat(),
            "clientIdentifier": "python-client"
        }
        response = requests.post(url, json=payload, auth=auth)
    else:
        params = {"client": "python-client"}
        response = requests.get(url, params=params, auth=auth)

    response.raise_for_status()
    return response.json()["shouldRun"]

# Usage
if should_schedule_run(schedule_id=1):
    print("Running scheduled job...")
    run_job()
else:
    print("Skipping job today")
```

### Cron Job Integration

```bash
# /etc/cron.d/payroll-schedule
# Run payroll check every weekday at 8:00 AM
0 8 * * 1-5 payroll /opt/scripts/check-and-run-payroll.sh >> /var/log/payroll.log 2>&1
```

```bash
#!/bin/bash
# /opt/scripts/check-and-run-payroll.sh

SCHEDULE_ID=1
API_URL="http://localhost:8080/api/v1/schedules/$SCHEDULE_ID/should-run"

if curl -s -f -u user:user "$API_URL?client=cron" | jq -e '.shouldRun == true' > /dev/null; then
    echo "[$(date)] Running payroll"
    /opt/payroll/run-payroll.sh
    exit_code=$?
    echo "[$(date)] Payroll completed with exit code $exit_code"
else
    echo "[$(date)] Skipping payroll - not scheduled"
fi
```

---

## Rate Limiting

*Not currently implemented - planned for future release.*

Expected implementation:
- Per-user rate limits: 100 requests/minute
- Per-schedule shouldRun queries: 1000 requests/hour
- HTTP 429 response when limit exceeded

---

## Versioning

Current API version: **v1**

Future versions will use separate URL paths:
- v1: `/api/v1/...`
- v2: `/api/v2/...` (future)

Backward compatibility maintained for at least one major version.

---

## Support

For issues or questions:
- GitHub Issues: [holiday-guard/issues](https://github.com/user/holiday-guard/issues)
- Documentation: See project README and module-specific READMEs

---

## Changelog

### v1.0.0 (Current)
- Initial API release
- Schedule CRUD operations
- shouldRun query endpoints
- Calendar view API
- Dashboard API
- Audit log API
- Version management
