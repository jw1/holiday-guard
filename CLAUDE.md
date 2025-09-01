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

### **Core API Endpoints** (Implemented)

#### **Daily Operations** (Primary Use Case)
```bash
# Simple "should I run today?" - defaults to today, no request body needed
GET /api/v1/schedules/{scheduleId}/should-run?client=payroll-service

# Advanced "should I run on specific date?" with full control
POST /api/v1/schedules/{scheduleId}/should-run
Body: {"queryDate": "2024-03-15", "clientIdentifier": "report-generator"}
```

#### **Schedule Management**
```bash
# Create schedule with initial rules
POST /api/v1/schedules
Body: {
  "name": "Payroll Schedule",
  "rules": [{"ruleType": "WEEKDAYS_ONLY", "effectiveFrom": "2024-01-01"}]
}

# Update schedule rules (creates new version automatically)  
POST /api/v1/schedules/{scheduleId}/versions
Body: {
  "rules": [{"ruleType": "WEEKDAYS_ONLY", "effectiveFrom": "2024-01-01"}],
  "overrides": [{"overrideDate": "2024-07-04", "action": "SKIP", "reason": "Independence Day"}]
}
```

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

## Established Patterns & Best Practices

### **Testing Patterns**
- **AssertJ Assertions**: Use `assertThat()` over JUnit assertions for better readability
- **BDD-Style Comments**: Structure tests with `// given`, `// when`, `// then` comments
- **Static Validators**: Use `@BeforeAll` with static validator for performance
- **Method Names**: Use descriptive, behavior-focused test method names
- **Test Data**: Use `List.of()` and `forEach()` for efficient bulk test data setup

### **Domain Object Patterns**
- **Lombok Builder**: Use `@Builder` with `@Builder.Default` for default field values
- **Modern Java**: Leverage `var`, method references, and modern Java features
- **Validation**: Combine JSR-303 annotations with custom business validation
- **Audit Fields**: Automatic timestamp handling with JPA lifecycle callbacks

### **Current Implementation Status**

#### ✅ **Completed Core Architecture** (Schedule Versioning System)
- **Domain Layer**: Complete entity model with 6 interconnected entities:
  - `Schedule` - Core schedule definitions
  - `ScheduleVersion` - Version tracking for audit trail
  - `ScheduleRules` - Rule definitions (WEEKDAYS_ONLY, CRON_EXPRESSION, CUSTOM_DATES, MONTHLY_PATTERN)
  - `ScheduleOverride` - Exception management (SKIP/FORCE_RUN actions)
  - `ScheduleMaterializedCalendar` - Pre-computed "should run" dates (performance optimization)
  - `ScheduleQueryLog` - Complete audit trail of all "should I run today?" queries

- **Repository Layer**: Full Spring Data JPA repositories with custom queries for all entities
- **Service Layer**: Core business logic implemented:
  - `ScheduleVersionService` - Automatic version management when rules change
  - `ScheduleQueryService` - Primary "should I run today?" query engine with override precedence
  - `ScheduleMaterializationService` - Rule engine with materialization pipeline
- **REST API Layer**: Production-ready endpoints:
  - `ShouldRunController` - Dead simple daily queries (GET for today, POST for specific dates)
  - `ScheduleVersionController` - Administrative rule management
  - `ScheduleController` - Full CRUD operations for schedules
- **Materialization Engine**: Complete rule processing system:
  - `RuleEngine` with pluggable handlers for all rule types
  - `OverrideApplicator` for SKIP/FORCE_RUN precedence logic
  - Handler implementations for WEEKDAYS_ONLY, CRON_EXPRESSION, CUSTOM_DATES, MONTHLY_PATTERN
- **Database Schema**: Flyway migrations (V001 + V002) with proper constraints and indexes
- **Test Infrastructure**: Comprehensive test coverage including:
  - Unit tests for all services (TDD approach)
  - Integration tests for repositories  
  - Controller tests with proper mocking
  - `ScheduleTestDataFactory` utility for consistent test data
  - Specialized test utilities like `ACHProcessingScheduleFactory`

#### ✅ **Key Design Patterns Established**
- **Materialized Calendar Strategy**: Store only "YES" dates, empty result = "NO" (performance optimization)
- **Override Precedence**: Overrides take priority over base calendar rules
- **Automatic Versioning**: Client rule updates trigger new versions automatically (no explicit version creation)
- **Complete Audit Trail**: Every query logged with version, reasoning, and client identifier
- **Date Boundary Validation**: 5-year future horizon, 1-year historical access
- **User-Friendly Defaults**: Missing dates default to "today" for ease of use
- **Factory Pattern for Schedule Utilities**: Established pattern for creating domain-specific schedule definitions (e.g., ACH processing schedules)

## Database Schema Guidelines
- **Table Naming**: Use snake_case (e.g., `schedules`, `holiday_rules`)
- **Primary Keys**: Use `id` as UUID with `GenerationType.UUID`
- **Foreign Keys**: Use `{referenced_table}_id` format (e.g., `schedule_id`)
- **Audit Fields**: Include `created_at` and `updated_at` TIMESTAMP fields with `@PrePersist/@PreUpdate`
- **Boolean Fields**: Use clear names like `is_active`, `is_holiday`
- **Indexes**: Add indexes for frequently queried columns
- **Constraints**: Use descriptive constraint names and `unique = true` for business keys

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

## Implementation Roadmap

### **Current TDD Phases** (Immediate Focus)
1. **✅ Phase 1: Domain Layer** - Schedule entity with validation and tests
2. **✅ Phase 2: Repository Layer** - Spring Data JPA with custom queries  
3. **✅ Phase 3: Service Layer** - Business logic and validation
4. **✅ Phase 4: REST API Layer** - HTTP endpoints with error handling

### **Feature Roadmap** (Long-term Goals)
1. **✅ Phase 1: US Holiday Foundation**
   - ✅ Schedule domain object with country field (US-first approach)
   - ✅ US federal holiday calculations via ACHProcessingScheduleFactory utility
   - ✅ Complete Schedule CRUD operations via REST API
   
2. **✅ Phase 2: Business Rules Engine**
   - ✅ Support multiple schedules with independent rules
   - ✅ Weekend pattern configuration (WEEKDAYS_ONLY)
   - ✅ Custom holiday overrides (per-schedule SKIP/FORCE_RUN actions)
   - ✅ Pluggable rule system (CRON_EXPRESSION, CUSTOM_DATES, MONTHLY_PATTERN)
   
3. **📋 Phase 3: Business Day Calculations**  
   - "Next N business days" endpoints
   - "Previous, next, next-next" style date generation
   - Auto-populate next year's holidays (cron or manual trigger)
   
4. **📋 Phase 4: International Expansion**
   - Multi-country holiday support (expand beyond US)
   - Timezone handling for multi-region deployments
   - Configurable weekend patterns by country
   
5. **📋 Phase 5: Production Ready**
   - Docker packaging with Postgres
   - Performance optimization and caching
   - Comprehensive API documentation

## Important: Calendar-Only Focus
**This project is concerned only with DATES (calendar days), not times of day.**
- No time-based processing (hours, minutes, cutoffs)
- No scheduling specific times within days  
- Focus purely on "which days should something run" not "what time of day"
- Rule engines, cron expressions, and schedules deal with date patterns only
- Business day navigation and "should run today?" queries are date-based only

## Dependencies
- **Core Framework**: Spring Boot 3.x with Spring Data JPA
- **Database**: H2 (development), designed for PostgreSQL (production)
- **Holiday Calculations**: Built-in US federal holiday calculations via ACHProcessingScheduleFactory
