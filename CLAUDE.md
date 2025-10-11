# Holiday Guard - Developer Notes

## Core Domain Model

Holiday Guard manages **Schedules** that determine when jobs should run. Each schedule has:
- **Rules** - Define the base pattern (weekdays, specific dates, cron expressions, etc.)
- **Deviations** - Override the rule for specific dates (force run or force skip)
- **Versions** - Immutable snapshots for audit history (each rule/deviation change creates a new version)

```
Schedule (1) ──→ (N) Version ──→ (1) Rule
                      │
                      └──→ (N) Deviation
```

### The Calendar Abstraction

The `Calendar` class encapsulates the "should run" business logic:
- Takes a Schedule, Rule, Deviations, and RuleEngine
- Provides `shouldRun(LocalDate)` - single date query
- Provides `shouldRun(LocalDate from, LocalDate to)` - date range query
- **Deviations always take precedence over rules** (FORCE_RUN/FORCE_SKIP)
- Used by both `ScheduleQueryService` and `CalendarViewService` to ensure algorithm consistency

```java
// RuleEngine evaluates the base rule
Calendar calendar = new Calendar(schedule, rule, deviations, ruleEngine::shouldRun);
boolean shouldRun = calendar.shouldRun(queryDate);
```

### Persistence Dependencies

When creating or updating entities, follow this order to satisfy foreign key constraints:

#### Initial Schedule Creation
```java
// 1. Save Schedule (needs primary key for version)
Schedule schedule = scheduleRepository.save(Schedule.builder()...build());

// 2. Save Version (needs schedule.id, generates version.id)
Version version = versionRepository.save(Version
    .builderFrom(schedule)
    .active(true)
    .build());

// 3. Save Rule (needs both schedule.id and version.id)
ruleRepository.save(Rule.builder()
    .scheduleId(schedule.getId())
    .versionId(version.getId())
    .ruleType(...)
    .build());

// 4. Save Deviations (needs schedule.id and version.id)
deviationRepository.saveAll(List.of(
    Deviation.builder()
        .scheduleId(schedule.getId())
        .versionId(version.getId())
        .deviationDate(...)
        .action(RunStatus.FORCE_RUN) // or FORCE_SKIP
        .reason("Emergency processing")
        .build()));
```

#### Schedule Update (Rule Change)
When a rule changes, create a new version:
```java
// 1. Deactivate old version
currentVersion.setActive(false);

// 2. Create new version using domain model factory
Version newVersion = Version.builderFrom(schedule).build();
newVersion.setActive(true);
Version savedVersion = versionRepository.save(newVersion);

// 3. Save new rule pointing to new version
ruleRepository.save(Rule.builder()
    .scheduleId(schedule.getId())
    .versionId(savedVersion.getId())
    .ruleType(...)
    .build());
```

## RunStatus and Data Flow

### RunStatus Enum
Represents the execution status for a schedule on a specific date:
- `RUN` - Rule says run (normal execution)
- `SKIP` - Rule says skip (normal non-execution)
- `FORCE_RUN` - Deviation overrides rule to force execution
- `FORCE_SKIP` - Deviation overrides rule to prevent execution

**Critical: Data Flow Direction**
- ✅ Correct: `RunStatus` (detailed) → `boolean shouldRun` (lossy conversion for convenience)
- ❌ Wrong: Trying to reconstruct `RunStatus` from `boolean` + string parsing (lossy, unreliable)

Always determine `RunStatus` from source data using `RunStatus.fromCalendar(shouldRun, deviation)`:
```java
// deviation.getAction() contains FORCE_RUN or FORCE_SKIP
RunStatus status = RunStatus.fromCalendar(shouldRun, deviationOpt.orElse(null));
boolean shouldRun = status.shouldRun(); // Derive boolean from status
```

### View DTO Hierarchy

Backend view DTOs follow a normalized structure to eliminate data redundancy:

**Calendar Views (Multi-Schedule Calendar Viewer)**
```
MultiScheduleCalendarView          // Top-level: yearMonth + list of schedules
└── ScheduleMonthView[]           // Per-schedule: id, name, yearMonth + days
    └── DayStatusView[]           // Atomic: date, status, reason (no schedule context)
```

This reduces JSON payload size by ~70% compared to a flat structure where schedule metadata repeats for every day.

**Dashboard Views (Today's Status)**
```
ScheduleDashboardView             // Per-schedule: id, name, status, shouldRun, reason
```
Includes both `RunStatus` (detailed) and `shouldRun` boolean (convenience).

### Domain Model Factory Methods

**Version.builderFrom()**
- Factory method for creating new versions
- Returns a `VersionBuilder` pre-configured with schedule context
- Encapsulates version creation logic in the domain model
- Usage: `Version.builderFrom(schedule).active(true).build()`

**Deviation.builderFrom()**
- Factory method for creating deviations with schedule/version context
- Usage: `Deviation.builderFrom(schedule, version).deviationDate(...).action(...).build()`

### Repository Architecture

The application supports two repository implementations, selected via Spring profiles:

#### H2 Profile (SQL Database)
- Full CRUD operations
- Audit logging (QueryLog tracking)
- Management UI enabled
- Uses JPA/Hibernate
- `DataProvider.supportsManagement()` returns `true`

#### JSON Profile (File-Based)
- Read-only operations
- No audit logging
- Management UI disabled (404 responses)
- Loads data from `app.repo.json.filename` (default: `./data.json`)
- `DataProvider.supportsManagement()` returns `false`
- All write operations throw `UnsupportedOperationException`

## Key Principles

1. **Calendar Abstraction**: Always use `Calendar` class for shouldRun logic - ensures consistency across services
2. **RunStatus First**: Determine `RunStatus` from source data, derive `boolean` from it (never reverse)
3. **Normalized Views**: Use hierarchical view DTOs to eliminate redundant schedule metadata
4. **Domain Model First**: Use factory methods like `Version.builderFrom()` for entity creation
5. **Save Order Matters**: Always save parent entities before children to satisfy FK constraints
6. **Version History**: Never update existing versions - create new ones for audit trail
7. **Profile-Based Features**: Management operations automatically disabled for read-only repositories

## Test Data

- **H2 Profile**: Uses `DataInitializer` to populate database on startup
- **JSON Profile**: Loads from `data.json` (or test-specific `test-data.json` in tests)
- Both contain 4 demo schedules: US Federal Holidays, UK Bank Holidays, Canadian Public Holidays, Australian Public Holidays

## Frontend TypeScript Types

The frontend TypeScript types mirror the backend view DTOs:

**Calendar Views (`calendar-view.ts`)**
```typescript
interface MultiScheduleCalendarView {
    yearMonth: string;
    schedules: ScheduleMonthView[];
}

interface ScheduleMonthView {
    scheduleId: number;
    scheduleName: string;
    yearMonth: string;
    days: DayStatusView[];
}

interface DayStatusView {
    date: string;  // ISO date string
    status: RunStatus;
    reason?: string;
}
```

**Dashboard Views (`backend.ts`)**
```typescript
interface ScheduleDashboardView {
    scheduleId: number;
    scheduleName: string;
    status: RunStatus;      // detailed enum
    shouldRun: boolean;     // convenience
    reason: string;
}
```

**Legacy types marked `@deprecated` for backwards compatibility during React component migration.**

## Naming Conventions

### US Federal Reserve Business Days
The rule type `US_FEDERAL_RESERVE_BUSINESS_DAYS` represents the standard banking calendar used for ACH processing and other Federal Reserve operations. Related classes use consistent naming:

- **Rule Type**: `US_FEDERAL_RESERVE_BUSINESS_DAYS`
- **Handler**: `USFederalReserveBusinessDaysHandler`
- **Factory**: `USFederalReserveScheduleFactory`

While this rule is commonly used for ACH processing, the naming focuses on the authoritative source (US Federal Reserve) rather than a specific application (ACH). This makes the rule reusable for any operation that follows Federal Reserve business days.
