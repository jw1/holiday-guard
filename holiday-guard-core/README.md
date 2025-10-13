# Holiday Guard Core Module

This module contains the business logic and service layer for Holiday Guard. It implements the rule evaluation engine, schedule management, and calendar operations.

## Service Layer

### ScheduleService
Manages schedule CRUD operations and version history.

**Key Methods:**
```java
Schedule createSchedule(CreateScheduleRequest request);
Schedule updateSchedule(Long id, UpdateScheduleRequest request);
Optional<Schedule> findScheduleById(Long id);
List<Schedule> findAllSchedules();
void deleteSchedule(Long id);
```

**Responsibilities:**
- Schedule creation and validation
- Name uniqueness enforcement
- Active status management

### ScheduleQueryService
Core service for evaluating whether schedules should run on specific dates.

**Key Methods:**
```java
ShouldRunQueryResponse shouldRunToday(Long scheduleId, ShouldRunQueryRequest request);
ShouldRunQueryResponse shouldRunOnDate(Long scheduleId, LocalDate date, String clientIdentifier);
```

**Responsibilities:**
- Loading active schedule version
- Building Calendar objects via RuleEngine
- Evaluating shouldRun logic
- Creating detailed RunStatus responses
- Query logging (H2 profile only)

**Response Structure:**
```java
public record ShouldRunQueryResponse(
    Long scheduleId,
    LocalDate queryDate,
    boolean shouldRun,        // Convenience boolean
    RunStatus runStatus,      // Detailed status (RUN, SKIP, FORCE_RUN, FORCE_SKIP)
    String reason,
    boolean deviationApplied,
    Long versionId
) {}
```

### ScheduleVersionService
Manages schedule version history and rule updates.

**Key Methods:**
```java
Version updateScheduleRule(Long scheduleId, UpdateRuleRequest request);
List<Version> getVersionHistory(Long scheduleId);
Version getActiveVersion(Long scheduleId);
```

**Responsibilities:**
- Creating new versions on rule changes
- Deactivating old versions
- Maintaining version audit trail
- Enforcing single-active-version constraint

### CalendarViewService
Generates multi-schedule calendar visualizations for the frontend.

**Key Methods:**
```java
MultiScheduleCalendarView getCalendarView(YearMonth yearMonth, List<Long> scheduleIds);
```

**Responsibilities:**
- Building Calendar objects for multiple schedules
- Generating date ranges for month views
- Creating normalized view DTOs (70% smaller payloads)
- Evaluating shouldRun for all dates in range

**Output Structure:**
```java
MultiScheduleCalendarView          // Top-level: yearMonth + schedules
└── ScheduleMonthView[]           // Per-schedule: id, name, days
    └── DayStatusView[]           // Atomic: date, status, reason
```

### CurrentUserService
Provides user context for audit fields (planned feature).

**Current Implementation:**
```java
public String getCurrentUser() {
    return "api-user"; // TODO: Extract from SecurityContext
}
```

**Future:** Will integrate with Spring Security to extract authenticated username.

## Rule Engine

The rule engine is the heart of Holiday Guard's schedule evaluation logic.

### RuleEngine Interface
```java
public interface RuleEngine {
    boolean shouldRun(Rule rule, LocalDate date);
    List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to);
}
```

### RuleEngineImpl
Coordinates multiple `RuleHandler` implementations via strategy pattern.

**Constructor:**
```java
@Service
public class RuleEngineImpl implements RuleEngine {
    public RuleEngineImpl(List<RuleHandler> handlers) {
        // Automatically discovers all RuleHandler beans
    }
}
```

**Dispatch Logic:**
```java
public boolean shouldRun(Rule rule, LocalDate date) {
    RuleHandler handler = getHandlerForType(rule.getRuleType());
    return handler.shouldRun(rule, date);
}
```

### RuleHandler Interface
Strategy interface implemented by each rule type.

```java
public interface RuleHandler {
    Rule.RuleType getSupportedRuleType();
    boolean shouldRun(Rule rule, LocalDate date);
    List<LocalDate> generateDates(Rule rule, LocalDate from, LocalDate to);
}
```

### Rule Handler Implementations

#### WeekdaysOnlyHandler
**Rule Type:** `WEEKDAYS_ONLY`
**Logic:** Returns true for Monday-Friday, false for Saturday-Sunday
**Config:** None required

#### AllDaysHandler
**Rule Type:** `ALL_DAYS`
**Logic:** Returns true for every day
**Use Case:** Schedules that need most days, with deviations for exceptions

#### NoDaysHandler
**Rule Type:** `NO_DAYS`
**Logic:** Returns false for every day
**Use Case:** Rarely-running schedules that use FORCE_RUN deviations for specific dates

#### CronExpressionHandler
**Rule Type:** `CRON_EXPRESSION`
**Logic:** Evaluates Spring's 6-field cron expressions
**Config:** Cron expression (e.g., `"0 0 0 * * *"` for daily at midnight)

**Format:** `sec min hour day month dow`
- Supports `*`, `?`, `,`, `-`, `/` special characters
- Time fields should be `0 0 0` since Holiday Guard is date-only

#### USFederalReserveBusinessDaysHandler
**Rule Type:** `US_FEDERAL_RESERVE_BUSINESS_DAYS`
**Logic:** Weekdays excluding US federal holidays
**Config:** None required

**Excluded Holidays:**
- Fixed: New Year's Day, Independence Day, Veterans Day, Christmas
- Floating: MLK Jr Day, Presidents' Day, Memorial Day, Labor Day, Columbus Day, Thanksgiving
- Recent: Juneteenth (2021+)

## Deviation System

### DeviationApplicator
Applies date-specific overrides to calendar evaluation.

```java
public interface DeviationApplicator {
    Optional<Deviation> findDeviationForDate(List<Deviation> deviations, LocalDate date);
}
```

**Logic:**
1. Check if a deviation exists for the query date
2. If deviation exists and not expired, return it
3. Deviation action (FORCE_RUN/FORCE_SKIP) overrides rule evaluation

**Precedence:** Deviations always take priority over rule logic.

## Utility Classes

### USFederalReserveScheduleFactory
Factory for creating complete US Federal Reserve business day schedules.

**Purpose:** Generates standard banking calendars used for ACH processing.

**Method:**
```java
FederalReserveScheduleDefinition createScheduleDefinition(int year);
```

**Output:**
- Schedule entity with name and description
- WEEKDAYS_ONLY rule
- FORCE_SKIP deviations for all federal holidays

**Inner Class - USFederalHolidays:**
```java
List<LocalDate> getHolidays(int year);  // Calculate all federal holidays
List<CreateDeviationRequest> createSkipDeviations(int year);  // Create deviation requests
```

### ACHProcessingScheduleFactory
*Note: May be deprecated in favor of USFederalReserveScheduleFactory*

## Exception Handling

### ScheduleNotFoundException
```java
throw new ScheduleNotFoundException(scheduleId);
```
- HTTP 404 in REST API
- Thrown when schedule ID doesn't exist

### DuplicateScheduleException
```java
throw new DuplicateScheduleException(scheduleName);
```
- HTTP 409 Conflict in REST API
- Thrown when creating schedule with existing name

### IllegalArgumentException
- Invalid rule configuration
- Query dates outside planning horizon (5 years future, 1 year past)
- Malformed cron expressions

## Key Design Patterns

### Strategy Pattern
Rule handlers implement a common interface, allowing the engine to dispatch to the correct handler at runtime.

### Factory Pattern
Domain entity builders (`Version.builderFrom()`, `Deviation.builderFrom()`) encapsulate construction logic.

### Service Layer Pattern
Business logic separated from controllers and repositories, enabling reuse and testability.

### Calendar Abstraction
The `Calendar` class centralizes shouldRun evaluation, ensuring consistent logic across all services.

## Testing

### Unit Tests
```java
@Test
void shouldRunReturnsTrueForWeekday() {
    WeekdaysOnlyHandler handler = new WeekdaysOnlyHandler();
    LocalDate monday = LocalDate.of(2025, 1, 6);
    assertTrue(handler.shouldRun(null, monday));
}
```

### Integration Tests
```java
@SpringBootTest
class ScheduleQueryServiceTest {
    @Autowired
    private ScheduleQueryService service;

    @Test
    void shouldQueryScheduleSuccessfully() {
        // Test with full Spring context
    }
}
```

## Dependencies

- **holiday-guard-domain** - Domain entities
- **holiday-guard-repository** - Data access interfaces
- **Spring Boot** - Dependency injection, transaction management
- **Spring Framework** - Scheduling support (CronExpression)

## Performance Considerations

- **Calendar Caching:** Calendar objects can be cached since they're immutable for a given version
- **Date Range Queries:** generateDates methods use Java streams for efficient date iteration
- **Handler Discovery:** Rule handlers are discovered once at startup via Spring DI
- **Query Logging:** Only enabled for H2 profile to avoid performance impact in production

## Future Enhancements

1. **User Context:** Replace hardcoded "api-user" with SecurityContext integration
2. **Rule Handler Registry:** Dynamic rule handler registration for custom rule types
3. **Caching:** Add Spring Cache abstraction for frequently-queried schedules
4. **Metrics:** Add execution time metrics for rule evaluation
