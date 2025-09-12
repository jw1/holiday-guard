# 🗓️ Holiday Guard

**Group/Package**: com.jw.holidayguard  
**Purpose**: Service for managing business calendars with holiday/weekend rules, overrides, and business day forecasting.

A company could use this to manage backend scheduling (e.g. ACH file builder runs on fed days, payroll on alternate Fridays with holiday adjustments).

## Features
Service for managing multiple business calendars with rules for:
- Weekends and US Federal/Bank holidays  
- ACH/payday/internal settlement days
- Custom overrides (per-calendar and global)
- Business day calculations and forecasting


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

### UI Development

For rapid UI development with hot-reloading, run the frontend and backend development servers separately:

1.  **Start Backend API**: In one terminal:
    ```bash
    ./backend.sh
    ```
2.  **Start Frontend Dev Server**: In another terminal (this will open the browser with hot-reloading):
    ```bash
    ./frontend.sh
    ```
    The UI will be accessible at `http://localhost:5173/`. The Vite development server is configured to proxy API requests to the backend running on `http://localhost:8080/`.

**Note on TailwindCSS v4:** For styling to apply correctly, ensure `src/index.css` contains `@import "tailwindcss";` and any desired plugins like `@plugin '@tailwindcss/forms';`. This setup was based on the guide [How to install Tailwind CSS v4 to your React project](https://tailkit.com/blog/how-to-install-tailwind-css-v4-to-your-react-project).

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

### **Core API Endpoints**
- **Daily Operations**: `GET /api/v1/schedules/{id}/should-run` (primary use case)
- **Schedule Management**: `POST /api/v1/schedules` and `POST /api/v1/schedules/{id}/versions`
- **Rule Types**: WEEKDAYS_ONLY, CRON_EXPRESSION, CUSTOM_DATES, MONTHLY_PATTERN
- **Override Actions**: SKIP, FORCE_RUN


## 🖥️ Frontend UI

The project includes a React-based administrative user interface, located in the `holiday-guard-react` module.

-   **Purpose**: Provides an admin interface for managing schedules and configurations.
-   **Technologies**: React, Vite (for fast development and bundling), TypeScript.
-   **Integration**: The UI is built into static assets and embedded directly into the Spring Boot application JAR, allowing for a single deployable artifact.

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

#### ✅ **Completed Core Architecture**
- **Domain Layer**: 6-entity model (Schedule, ScheduleVersion, ScheduleRules, ScheduleOverride, ScheduleMaterializedCalendar, ScheduleQueryLog)
- **Service Layer**: ScheduleVersionService, ScheduleQueryService, ScheduleMaterializationService
- **REST API Layer**: ShouldRunController, ScheduleVersionController, ScheduleController
- **Materialization Engine**: RuleEngine with pluggable handlers, OverrideApplicator
- **Database Schema**: Flyway migrations with proper constraints/indexes
- **Test Infrastructure**: Comprehensive TDD coverage with test utilities

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

### **Implementation Status**
✅ **Complete**: Core architecture (Domain/Service/API layers), Business rules engine, US holiday support  
📋 **Next**: Business day calculations, International expansion, Production deployment

## Important: Calendar-Only Focus
**This project is concerned only with DATES (calendar days), not times of day.**
- No time-based processing (hours, minutes, cutoffs)
- No scheduling specific times within days  
- Focus purely on "which days should something run" not "what time of day"
- Rule engines, cron expressions, and schedules deal with date patterns only
- Business day navigation and "should run today?" queries are date-based only

## Tech Stack
- **Framework**: Java 21 + Spring Boot 3.x + Spring Data JPA + Flyway
- **Database**: H2 (dev) / PostgreSQL (prod)
- **US Holidays**: Built-in calculations via ACHProcessingScheduleFactory
