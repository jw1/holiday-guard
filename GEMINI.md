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
APIs use a `/api/v1/` base path and follow standard REST conventions. A consistent JSON error structure is used for all endpoints.

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
The project uses a standard testing pyramid with unit, integration, and web layer tests. Naming conventions are `*Test.java`, `*IntegrationTest.java`, and `*ControllerTest.java` respectively. Aim for >80% test coverage on business logic.

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
- **Domain Layer**: 6-entity model (Schedule, ScheduleVersion, ScheduleRule, ScheduleOverride, ScheduleMaterializedCalendar, ScheduleQueryLog)
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
All new features should follow the standard Red-Green-Refactor TDD cycle.

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

## 🤖 Working with Gemini

As an AI assistant, I can help you with various tasks related to this project. I am aware of the project's structure, conventions, and development workflow as outlined in this document. Here are some ways you can interact with me:

*   **Running Commands**: You can ask me to execute any of the development commands listed, such as running tests (`./mvnw clean test`), starting the application (`./mvnw spring-boot:run`), or building the project (`./mvnw clean package`).
*   **Implementing Features**: I can help you add new features by following the established TDD development flow. For example, you can ask me to "add a new feature to support X, following the TDD process."
*   **Adhering to Conventions**: I will follow the established API design guidelines, testing strategies, and coding patterns when making changes to the codebase.
*   **Code Comprehension**: You can ask me to explain parts of the code, and I will use my understanding of the project to provide you with a detailed explanation.
*   **Database Migrations**: I can help create new Flyway migration scripts that follow the project's database schema guidelines.