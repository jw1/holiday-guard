# 🗓️ Claude Calendar Service

Project Overview

Name: Holiday Guard
Group/Package: com.jw.holidayguard
Purpose: A service for managing business calendars with holiday and weekend rules, overrides, and forecasting of future business days.

A company could use this to manage all their backend stuff (e.g. maybe the ach file builder only runs on fed days and the payroll software only runs on every other friday, moving ahead a business day if that day is a holiday).  

This project begins with a simple bank holiday service, but is designed to grow into a flexible, cloud-ready calendar management platform.


## Purpose
Service for managing multiple business calendars with rules for:
- Weekends
- US Federal holidays
- Bank holidays
- ACH/payday/internal settle days
- Custom overrides (per calendar or global)

## Features
- Multiple named calendars
- Rule-based exclusion of days
- Override support (per-calendar and global)
- Generate "next N business days"
- Generate "previous, next, next-next" style dates
- Auto-populate next year in December (cron or manual trigger)
- Docker-ready deployment with Postgres
- Cloud-friendly (stateless core + database backend)

## Tech
- Java 21 + Spring Boot 3.x
- Spring Data JPA + Postgres
- Flyway migrations
- Exposed via REST API

## Development Commands
```bash
# Run tests
./mvnw clean test

# Start application
./mvnw spring-boot:run

# Build JAR
./mvnw clean package

# Run with profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Design Guidelines
- **Base Path**: `/api/v1/` prefix for all endpoints
- **HTTP Methods**: 
  - GET for read operations
  - POST for create operations
  - PUT for update operations
  - DELETE for delete operations
- **Request/Response Format**: JSON
- **Error Response Structure**:
  ```json
  {
    "error": "ERROR_CODE",
    "message": "Human readable message",
    "timestamp": "2024-01-01T00:00:00Z"
  }
  ```
- **Resource Naming**: Use plural nouns (`/calendars`, `/holidays`)

## Testing Strategy
- **Unit Tests**: Test business logic in service layer
  - Naming: `ClassNameTest` (e.g., `BusinessDayServiceTest`)
  - Mock external dependencies
- **Integration Tests**: Test repository layer with database
  - Naming: `ClassNameIntegrationTest`
  - Use `@DataJpaTest` for JPA repositories
- **Web Layer Tests**: Test REST controllers
  - Naming: `ClassNameControllerTest`
  - Use `@WebMvcTest` for controller testing
- **Test Coverage**: Aim for >80% coverage on business logic
- **Test Data**: Use builders or factories for consistent test data

## Database Schema Guidelines
- **Table Naming**: Use snake_case (e.g., `business_calendars`, `holiday_rules`)
- **Primary Keys**: Use `id` as BIGINT with auto-increment
- **Foreign Keys**: Use `{referenced_table}_id` format (e.g., `calendar_id`)
- **Audit Fields**: Include `created_at` and `updated_at` TIMESTAMP fields
- **Boolean Fields**: Use clear names like `is_active`, `is_holiday`
- **Indexes**: Add indexes for frequently queried columns
- **Constraints**: Use descriptive constraint names following pattern: `{table}_{column}_{constraint_type}`

## TDD Development Flow
Follow the Red-Green-Refactor cycle for all new features:

1. **🔴 RED**: Write a failing test
   - Write the simplest test that captures the requirement
   - Run test to ensure it fails for the right reason
   - Commit failing test with message: "RED: [test description]"

2. **🟢 GREEN**: Make the test pass
   - Write minimal code to make the test pass
   - Don't worry about code quality yet
   - Run tests to ensure they pass
   - Commit passing code with message: "GREEN: [implementation description]"

3. **🔄 REFACTOR**: Improve the code
   - Clean up implementation while keeping tests green
   - Extract methods, improve naming, remove duplication
   - Run tests after each change
   - Commit refactored code with message: "REFACTOR: [improvement description]"

**Repeat for each small increment of functionality.**

## Roadmap
1. Bootstrapped with US federal holiday set (via [bank-holidays](https://github.com/your-org/bank-holidays))
2. Support multiple calendars with independent rules
3. Expose CRUD for calendars, rules, overrides
4. Business-day calculation endpoints
5. Scheduling for auto-populate jobs
6. Packaging with Docker
