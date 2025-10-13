# 🗓️ Holiday Guard

A flexible schedule management service for business processes that need more than simple cron expressions.

## 💡 Inspiration & Use Case

Many business processes run on schedules that aren't perfectly regular:
- **Payroll systems** that skip bank holidays but need emergency runs on weekends
- **ETL pipelines** that follow business calendars with regional variations
- **Report generation** that runs monthly except during quarter-end blackouts
- **Data processing** that needs one-off schedule adjustments without code changes

Traditional cron expressions can't easily handle:
- "Run on weekdays except holidays"
- "First business day of the month (accounting for holidays)"
- "Force run this Saturday for emergency processing"
- Sharing schedule information across multiple applications

**Holiday Guard solves this** by providing a centralized schedule service where business calendars are **managed, versioned, and queryable via HTTP API**. Applications simply ask "Should I run today?" instead of embedding complex calendar logic.

The service is designed to run either:
- **As an HTTP service** - Apps query via REST API (primary use case)
- **As a CLI utility** - For standalone scripts and command-line workflows *(coming soon)*

---

*The majority of this codebase was created using Anthropic Claude and Google Gemini as an evaluation of AI coding tools.*  

## 🚀 Quick Start

```bash
# Run tests
./mvnw clean test

# Start application with H2 database (management UI enabled)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# Or start with JSON file backend (read-only)
./mvnw spring-boot:run -Dspring-boot.run.profiles=json

# Application runs on http://localhost:8080
# Admin UI available at http://localhost:8080
```

**Default Profile**: H2 (in-memory SQL database with full CRUD and management UI)

## 📋 Primary Use Case

Daily processes query the service to determine if they should run:

```bash
# Simple: Should I run today?
curl "http://localhost:8080/api/v1/schedules/{scheduleId}/should-run?client=payroll-service"

# Advanced: Should I run on specific date?
curl -X POST "http://localhost:8080/api/v1/schedules/{scheduleId}/should-run" \
  -H "Content-Type: application/json" \
  -d '{"queryDate": "2024-03-15", "clientIdentifier": "report-generator"}'
```

Response:
```json
{
  "scheduleId": 1,
  "queryDate": "2024-03-15",
  "shouldRun": true,
  "runStatus": "RUN",
  "reason": "Scheduled to run - rule matches",
  "deviationApplied": false,
  "versionId": 10
}
```

**RunStatus values**: `RUN`, `SKIP`, `FORCE_RUN`, `FORCE_SKIP` - detailed status with deviation info

## 🖥️ Admin UI & API

**Web UI** (H2 profile only)
- Schedule management (create, edit, view)
- Calendar visualization (multi-schedule month view)
- Deviation management (force run/skip specific dates)
- Dashboard (today's status for all active schedules)
- Audit logs (query history)

**REST API Endpoints**
- `GET /api/v1/schedules/{id}/should-run` - Query if schedule should run
- `GET /api/v1/schedules` - List all schedules
- `GET /api/v1/calendar-view` - Multi-schedule calendar data
- `GET /api/v1/dashboard/schedule-status` - Today's status for all schedules
- `GET /api/v1/audit-logs` - Query audit trail
- `POST /api/v1/schedules` - Create schedule (H2 only)
- `PUT /api/v1/schedules/{id}` - Update schedule (H2 only)

## 🏗️ Architecture

- **Spring Boot 3.x** + **Java 21** + **React** admin UI
- **Pluggable Repository**: H2 (SQL with CRUD) or JSON (read-only file)
- **Schedule Versioning**: Complete audit trail of rule changes
- **Calendar Domain Object**: Encapsulates shouldRun business logic with rule evaluation
- **Deviation System**: Override rules for specific dates (force run or skip)
- **Boundary Validation**: 5-year planning horizon, 1-year historical queries
- **Normalized View DTOs**: Efficient data structure (70% smaller JSON payloads)

## 🔧 Core Concepts

### Entities
- **Schedule**: Named business calendar (e.g., "Payroll Schedule", "ACH Processing")
- **Rule**: Base pattern - weekdays, cron expressions, specific dates, monthly patterns, US Federal Reserve business days
- **Deviation**: Date-specific overrides - `FORCE_RUN` or `FORCE_SKIP` with reason
- **Version**: Immutable snapshots for audit trail (each rule/deviation change creates new version)
- **QueryLog**: Complete audit trail of all shouldRun queries (H2 profile only)

### Repository Profiles

**H2 Profile** (default)
- In-memory SQL database
- Full CRUD operations
- Management UI enabled
- Query logging and audit trails
- Best for: Development, testing, production deployments

**JSON Profile**
- File-based read-only storage
- Loads from `data.json`
- Management UI disabled (404 responses)
- No audit logging
- Best for: Simple deployments, embedded use cases, CI/CD environments

## 📖 Developer Documentation

- **[CLAUDE.md](./CLAUDE.md)** - Core architecture, domain model, data flow patterns, and key principles
- **[docs/schedule_service_design.md](./docs/schedule_service_design.md)** - Detailed service design and data flow *(if available)*

## 🧪 Testing

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=ShouldRunControllerTest

# Integration tests only
./mvnw test -Dtest=*RepositoryTest
```

**Test Data**: The application includes 4 demo schedules:
- US Federal Reserve Business Days (used for ACH processing, payroll, etc.)
- UK Bank Holidays
- Canadian Public Holidays
- Australian Public Holidays

Test utilities available in `ScheduleTestDataFactory` and `USFederalReserveScheduleFactory` for common patterns.

## 🏷️ License

This project is intended for internal business use.
