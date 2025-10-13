# Holiday Guard Domain Module

This module contains the core domain entities and business logic for Holiday Guard. It represents the heart of the application's data model and encapsulates the fundamental concepts of schedule management.

## Domain Entities

### Schedule
Represents a named business calendar (e.g., "Payroll Schedule", "ACH Processing").

**Key Fields:**
- `id` - Primary key
- `name` - Unique schedule name
- `description` - Human-readable description
- `country` - Country code (default: "US")
- `active` - Whether schedule is currently in use
- Audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

**Relationships:**
- Has many `Version` records (audit history)
- Each version has one `Rule` and many `Deviation` records

**Factory Constructor:**
```java
Schedule schedule = Schedule.builder()
    .name("Payroll Schedule")
    .description("US payroll processing calendar")
    .active(true)
    .build();
```

### Version
Immutable audit snapshots of schedule configuration. Every rule or deviation change creates a new version.

**Key Fields:**
- `id` - Primary key
- `scheduleId` - Foreign key to schedule
- `versionNumber` - Sequential version counter
- `active` - Only one version per schedule can be active
- `effectiveFrom` - When this version takes effect
- Audit fields: `createdAt`, `createdBy`

**Factory Method:**
```java
Version newVersion = Version.builderFrom(schedule)
    .active(true)
    .build();
```

This factory method automatically sets the schedule ID and creates the next version number.

### Rule
Defines the base pattern for when a schedule should run.

**Key Fields:**
- `id` - Primary key
- `scheduleId` - Foreign key to schedule
- `versionId` - Foreign key to version
- `ruleType` - Type of rule (enum)
- `ruleConfig` - Optional configuration string
- `effectiveFrom` - When rule becomes active

**Rule Types:**
- `WEEKDAYS_ONLY` - Monday through Friday
- `ALL_DAYS` - Every day (use with deviations)
- `NO_DAYS` - Never runs (use with FORCE_RUN deviations)
- `CRON_EXPRESSION` - Uses 6-field Spring cron format (sec min hour day month dow)
- `US_FEDERAL_RESERVE_BUSINESS_DAYS` - Weekdays excluding US federal holidays
- `SPECIFIC_DATES` - Comma-separated ISO dates in ruleConfig

**Example:**
```java
Rule rule = Rule.builder()
    .scheduleId(schedule.getId())
    .versionId(version.getId())
    .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
    .effectiveFrom(LocalDate.now().atStartOfDay())
    .build();
```

### Deviation
Date-specific overrides that take precedence over rules.

**Key Fields:**
- `id` - Primary key
- `scheduleId` - Foreign key to schedule
- `versionId` - Foreign key to version
- `deviationDate` - The date to override
- `action` - `FORCE_RUN` or `FORCE_SKIP` (RunStatus enum)
- `reason` - Human-readable explanation
- `expiresAt` - Optional expiration timestamp

**Factory Method:**
```java
Deviation deviation = Deviation.builderFrom(schedule, version)
    .deviationDate(LocalDate.of(2025, 12, 25))
    .action(RunStatus.FORCE_SKIP)
    .reason("Christmas Day")
    .build();
```

### RunStatus (Enum)
Represents the execution status for a schedule on a specific date.

**Values:**
- `RUN` - Rule says run (normal execution)
- `SKIP` - Rule says skip (normal non-execution)
- `FORCE_RUN` - Deviation overrides rule to force execution
- `FORCE_SKIP` - Deviation overrides rule to prevent execution

**Important:** Always derive `boolean shouldRun` from `RunStatus`, never try to reconstruct `RunStatus` from a boolean.

```java
RunStatus status = RunStatus.fromCalendar(shouldRun, deviationOpt.orElse(null));
boolean shouldRun = status.shouldRun(); // Derive boolean from status
```

### Calendar
Encapsulates the "should run" business logic. This is the primary interface for querying schedule behavior.

**Key Concept:** Deviations always take precedence over rules.

**Constructor:**
```java
Calendar calendar = new Calendar(schedule, rule, deviations, ruleEngine::shouldRun);
```

**Usage:**
```java
// Single date query
boolean shouldRun = calendar.shouldRun(queryDate);

// Date range query
Map<LocalDate, Boolean> results = calendar.shouldRun(fromDate, toDate);
```

The Calendar class ensures algorithm consistency across all services by centralizing the shouldRun evaluation logic.

### QueryLog
Audit trail of all `shouldRun` queries (H2 profile only).

**Key Fields:**
- `id` - Primary key
- `scheduleId` - Which schedule was queried
- `versionId` - Which version was active at query time
- `queryDate` - Date being queried
- `result` - Whether schedule should run (boolean)
- `clientIdentifier` - Who made the query
- `queryTimestamp` - When query was made

## Entity Relationships

```
Schedule (1) ──→ (N) Version ──→ (1) Rule
                      │
                      └──→ (N) Deviation

Schedule (1) ──→ (N) QueryLog
```

## Persistence Order

When creating or updating entities, follow this order to satisfy foreign key constraints:

### Initial Schedule Creation
```java
// 1. Save Schedule
Schedule schedule = scheduleRepository.save(Schedule.builder()
    .name("Payroll Schedule")
    .build());

// 2. Save Version
Version version = versionRepository.save(Version
    .builderFrom(schedule)
    .active(true)
    .build());

// 3. Save Rule
ruleRepository.save(Rule.builder()
    .scheduleId(schedule.getId())
    .versionId(version.getId())
    .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
    .build());

// 4. Save Deviations
deviationRepository.saveAll(deviations);
```

### Schedule Update (Rule Change)
```java
// 1. Deactivate old version
currentVersion.setActive(false);

// 2. Create new version
Version newVersion = Version.builderFrom(schedule).build();
newVersion.setActive(true);
Version savedVersion = versionRepository.save(newVersion);

// 3. Save new rule
ruleRepository.save(Rule.builder()
    .scheduleId(schedule.getId())
    .versionId(savedVersion.getId())
    .ruleType(...)
    .build());
```

## Key Principles

1. **Immutable Versions** - Never update existing versions, always create new ones for audit history
2. **Single Active Version** - Only one version per schedule can be active at a time
3. **Deviations Override Rules** - Deviations (FORCE_RUN/FORCE_SKIP) always take precedence
4. **RunStatus First** - Determine RunStatus from source data, derive boolean from it (never reverse)
5. **Factory Methods** - Use `Version.builderFrom()` and `Deviation.builderFrom()` for proper entity creation

## JPA Configuration

All entities use:
- `@Entity` for JPA persistence
- `@Data` for Lombok getters/setters
- `@Builder` for fluent construction
- `@NoArgsConstructor` and `@AllArgsConstructor` for JPA and builder compatibility
- Audit fields with `@PrePersist` and `@PreUpdate` lifecycle callbacks

## Dependencies

- Jakarta Persistence API (JPA)
- Lombok (code generation)
- No business logic dependencies (pure domain model)
