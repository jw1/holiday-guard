# Holiday Guard - Developer Notes

## Entity Relationships & Save Order

### Core Domain Model
The application manages **Schedules** that determine when jobs should run based on **Rules** and **Deviations**. Each change to a schedule's configuration creates a new **Version** for audit history.

```
Schedule (1) ──→ (N) Version ──→ (1) Rule
                      │
                      └──→ (N) Deviation
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
//    Conceptually makes sense after rule, though could be before
overrideRepository.saveAll(List.of(
    Deviation.builder()
        .scheduleId(schedule.getId())
        .versionId(version.getId())
        .overrideDate(...)
        .action(...)
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

### Domain Model Factory Methods

**Version.builderFrom()**
- Factory method for creating new versions
- Returns a `VersionBuilder` pre-configured with schedule context
- Encapsulates version creation logic in the domain model
- Usage: `Version.builderFrom(schedule).active(true).build()`

**Rule.builderFrom()** *(if implemented)*
- Factory method for creating rules from request DTOs
- Centralizes rule creation logic

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

### Key Principles

1. **Domain Model First**: Use factory methods like `Version.builderFrom()` instead of manual builder construction
2. **Save Order Matters**: Always save parent entities before children to satisfy FK constraints
3. **Version History**: Never update existing versions - create new ones for audit trail
4. **Profile-Based Features**: Management operations automatically disabled for read-only repositories
5. **Deviations After Rules**: While technically independent, saving deviations after rules makes conceptual sense

## Test Data

- **H2 Profile**: Uses `DataInitializer` to populate database on startup
- **JSON Profile**: Loads from `data.json` (or test-specific `test-data.json` in tests)
- Both contain 4 demo schedules: US Federal Holidays, UK Bank Holidays, Canadian Public Holidays, Australian Public Holidays
