# 🗓️ Holiday Guard

A Spring Boot service for managing business calendars with holiday rules, overrides, and schedule queries.

The majority of code was created using Anthropic Claude and Google Gemini as an evaluation of coding CLI tools.  

## 🚀 Quick Start

```bash
# Run tests
./mvnw clean test

# Start application
./mvnw spring-boot:run

# Application runs on http://localhost:8080
```

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
  "scheduleId": "uuid",
  "queryDate": "2024-03-15",
  "shouldRun": true,
  "reason": "Scheduled to run - found in materialized calendar",
  "overrideApplied": false,
  "versionId": "uuid"
}
```

## 🏗️ Architecture

- **Spring Boot 3.x** + **Java 21**
- **PostgreSQL** with Flyway migrations  
- **Schedule Versioning**: Complete audit trail of rule changes
- **Materialized Calendar**: Pre-computed dates for performance
- **Override System**: Skip or force-run specific dates
- **Boundary Validation**: 5-year planning horizon

## 🔧 Configuration

The service uses these core entities:
- **Schedule**: Named calendar (e.g., "Payroll Schedule")
- **ScheduleRule**: When to run (weekdays, cron, custom dates, monthly patterns)
- **ScheduleOverride**: Exceptions (skip holidays, emergency runs)
- **MaterializedCalendar**: Pre-computed "should run" dates
- **QueryLog**: Complete audit trail

## 📖 Full Documentation

See [GEMINI.md](./GEMINI.md) for complete architecture details, API documentation, and development guidelines.

See [docs/schedule_service_design.md](./docs/schedule_service_design.md) for detailed service design and data flow.

## 🧪 Testing

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=ShouldRunControllerTest

# Integration tests only
./mvnw test -Dtest=*RepositoryTest
```

Test utilities available in `src/test/java/com/jw/holidayguard/util/ScheduleTestDataFactory.java` for common schedule types:
- Payroll schedules (weekly, bi-weekly)
- ACH processing schedules  
- Monthly/quarterly reports
- Custom date patterns

## 🏷️ License

This project is intended for internal business use.
