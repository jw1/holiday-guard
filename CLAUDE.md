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

## CLI Module Architecture

Holiday Guard includes a command-line interface module (`holiday-guard-cli`) that provides lightweight schedule queries without requiring the full web application.

### Design Principles
- **Zero Spring Dependencies**: CLI module depends only on `holiday-guard-core` (domain + services)
- **Manual Dependency Wiring**: Rule handlers instantiated directly (no Spring DI)
- **JSON Configuration**: File-based schedule definitions (`schedules.json`)
- **Fast Startup**: ~200-300ms (vs ~2-3s for web server)
- **Shell Integration**: Exit codes for scripting (0=run, 1=skip, 2=error)

### CLI vs Web Server Mode

| Feature | Web Server | CLI |
|---------|-----------|-----|
| Startup Time | ~2-3 seconds | ~200-300ms |
| Dependencies | Full Spring Boot | Core domain only |
| Configuration | H2/JSON profiles | JSON file |
| Management UI | Yes (H2 profile) | No |
| Query Logging | Yes (H2 profile) | No |
| Best For | Multiple clients, UI, audit | Scripts, cron jobs, CI/CD |

### CLI Architecture

**Module**: `holiday-guard-cli`

**Key Classes**:
- `HolidayGuardCLI` - Main entry point (picocli command)
- `CLIScheduleService` - Builds Calendar objects from JSON config
- `CLIConfigLoader` - Loads JSON schedule definitions
- `CLIConfig` - JSON configuration model

**Dependency Wiring**:
```java
public CLIScheduleService() {
    // Manually instantiate all rule handlers (no Spring)
    List<RuleHandler> handlers = List.of(
        new WeekdaysOnlyHandler(),
        new CronExpressionHandler(),
        new USFederalReserveBusinessDaysHandler(),
        new AllDaysHandler(),
        new NoDaysHandler()
    );
    this.ruleEngine = new RuleEngineImpl(handlers);
}
```

### CLI Configuration Format

`schedules.json`:
```json
{
  "schedules": [
    {
      "name": "Payroll Schedule",
      "description": "Runs on weekdays except holidays",
      "rule": {
        "ruleType": "WEEKDAYS_ONLY"
      },
      "deviations": [
        {
          "date": "2025-12-25",
          "action": "FORCE_SKIP",
          "reason": "Christmas Day"
        }
      ]
    }
  ]
}
```

### CLI Usage Examples

```bash
# Query if schedule should run today
java -jar holiday-guard-cli.jar "Payroll Schedule"

# Query specific date
java -jar holiday-guard-cli.jar "Payroll Schedule" --date 2025-12-25

# Quiet mode (exit code only)
java -jar holiday-guard-cli.jar "Payroll Schedule" --quiet
echo $?  # 0 = run, 1 = skip, 2 = error

# Shell script integration
if java -jar holiday-guard-cli.jar "Nightly Backup" --quiet; then
    echo "Running backup..."
    /opt/backup-script.sh
else
    echo "Skipping backup"
fi
```

See [holiday-guard-cli/README.md](./holiday-guard-cli/README.md) for complete documentation.

## Testing Patterns

### Test Organization

Tests are organized by module and type:

```
holiday-guard-domain/src/test/java/
├── domain/                    # Domain entity tests
│   ├── ScheduleTest.java
│   ├── CalendarTest.java
│   └── VersionTest.java

holiday-guard-core/src/test/java/
├── service/                   # Service layer tests
│   ├── ScheduleServiceTest.java
│   └── ScheduleQueryServiceTest.java
├── service/rule/handler/      # Rule handler tests
│   ├── WeekdaysOnlyHandlerTest.java
│   ├── CronExpressionHandlerTest.java
│   ├── USFederalReserveBusinessDaysHandlerTest.java
│   ├── AllDaysHandlerTest.java
│   └── NoDaysHandlerTest.java
└── util/                      # Utility tests
    └── ACHProcessingScheduleFactoryTest.java

holiday-guard-rest/src/test/java/
├── controller/                # Controller tests
│   ├── ScheduleControllerTest.java
│   ├── ScheduleControllerSecurityTest.java
│   └── DashboardControllerTest.java
└── exception/                 # Exception handler tests
    └── GlobalExceptionHandlerTest.java

holiday-guard-cli/src/test/java/
├── CLIConfigLoaderTest.java   # JSON configuration tests
├── CLIScheduleServiceTest.java # Calendar building tests
└── HolidayGuardCLITest.java   # CLI integration tests
```

### Test Types and Coverage

**Current Test Statistics** (as of v1.0):
- Total Tests: 164 (137 passing, 5 disabled, 27 added in CLI)
- Test Coverage: ~85% (domain, core, CLI fully covered)
- Disabled Tests: 5 in `ShouldRunControllerTest` (MockMvc security issues)

### Unit Test Patterns

**Rule Handler Tests**:
```java
@Test
void shouldGenerateWeekdaysForFullWeek() {
    // given - A date range spanning a full week
    LocalDate monday = LocalDate.of(2025, 1, 6);
    LocalDate sunday = LocalDate.of(2025, 1, 12);

    // when - Generating dates
    List<LocalDate> result = handler.generateDates(null, monday, sunday);

    // then - Only weekdays are included (5 days)
    assertEquals(5, result.size());
}
```

**Service Tests**:
```java
@SpringBootTest
class ScheduleQueryServiceTest {
    @Autowired
    private ScheduleQueryService service;

    @Test
    void shouldQueryScheduleSuccessfully() {
        // given - A schedule exists
        // when - Querying shouldRun
        ShouldRunQueryResponse response = service.shouldRunToday(1L, request);

        // then - Response contains correct status
        assertNotNull(response);
        assertEquals(RunStatus.RUN, response.runStatus());
    }
}
```

### Integration Test Patterns

**Controller Tests with MockMvc**:
```java
@WebMvcTest(controllers = ShouldRunController.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ShouldRunControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleQueryService service;

    @Test
    void shouldRunTodayReturnsOk() throws Exception {
        // given - Service returns valid response
        when(service.shouldRunToday(eq(1L), any()))
            .thenReturn(response);

        // when - Making GET request
        mockMvc.perform(get("/api/v1/schedules/1/should-run")
                .with(user("user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shouldRun").value(true));
    }
}
```

**Repository Tests**:
```java
@DataJpaTest
class ScheduleRepositoryTest {
    @Autowired
    private ScheduleRepository repository;

    @Test
    void shouldFindScheduleByName() {
        // given - A schedule is saved
        Schedule schedule = repository.save(Schedule.builder()
            .name("Test Schedule")
            .build());

        // when - Finding by name
        Optional<Schedule> found = repository.findByName("Test Schedule");

        // then - Schedule is found
        assertTrue(found.isPresent());
        assertEquals(schedule.getId(), found.get().getId());
    }
}
```

### Test Data Factories

**ScheduleTestDataFactory**:
```java
public class ScheduleTestDataFactory {
    public static Schedule createDefaultSchedule() {
        return Schedule.builder()
            .name("Test Schedule")
            .description("Test description")
            .country("US")
            .active(true)
            .build();
    }
}
```

**USFederalReserveScheduleFactory**:
```java
// Generates complete ACH schedule with federal holidays
FederalReserveScheduleDefinition definition =
    USFederalReserveScheduleFactory.createScheduleDefinition(2025);
```

### CLI Testing Approach

CLI tests don't use Spring context:

```java
class HolidayGuardCLITest {
    @Test
    void cli_shouldReturnExitCode0ForScheduleThatShouldRun(@TempDir Path tempDir) {
        // given - JSON config file
        File configFile = createConfigFile(tempDir, json);

        // when - Calling CLI directly
        HolidayGuardCLI cli = new HolidayGuardCLI();
        cli.scheduleName = "Test Schedule";
        cli.dateInput = "2025-10-13";
        cli.configFile = configFile;
        cli.quiet = true;

        int exitCode = cli.call();

        // then - Exit code is 0 (run)
        assertThat(exitCode).isEqualTo(0);
    }
}
```

### Testing Best Practices

1. **Arrange-Act-Assert (AAA)**: Structure tests with clear Given-When-Then comments
2. **Test Names**: Use descriptive names (`shouldReturnTrueForWeekday`)
3. **One Assertion Per Concept**: Focus each test on a single behavior
4. **Test Data Isolation**: Use `@TempDir` for file-based tests
5. **Mock External Dependencies**: Mock services in controller tests
6. **Integration vs Unit**: Use `@WebMvcTest` for controllers, `@DataJpaTest` for repositories
7. **Security Testing**: Test both success and authorization failure cases

### Running Tests

```bash
# All tests
./mvnw test

# Specific module
./mvnw test -pl holiday-guard-core

# Specific test class
./mvnw test -Dtest=WeekdaysOnlyHandlerTest

# Specific test method
./mvnw test -Dtest=WeekdaysOnlyHandlerTest#shouldGenerateWeekdaysForFullWeek

# Skip tests during build
./mvnw package -DskipTests
```

### Known Test Issues

**Disabled Tests**:
- `ShouldRunControllerTest` (5 tests) - MockMvc security context configuration issues
- Planned fix: Update security test configuration to match `ScheduleControllerSecurityTest` pattern

### Test Coverage Goals

- **Domain**: 100% coverage (simple POJOs)
- **Core Services**: 90%+ coverage (business logic critical)
- **Rule Handlers**: 100% coverage (all rule types tested)
- **Controllers**: 80%+ coverage (happy path + error cases)
- **Integration**: Key user workflows covered

## Troubleshooting Guide

### Common Issues

**Issue: "Schedule not found"**
- Check schedule ID exists: `GET /api/v1/schedules`
- Verify schedule is active: `"active": true`
- Check case sensitivity for name-based lookups

**Issue: "Configuration file not found" (CLI)**
- Default location: `./schedules.json`
- Use `--config` flag to specify custom path
- Verify JSON syntax is valid

**Issue: Cron expression parsing fails**
- Use 6-field format: `sec min hour day month dow`
- Example: `0 0 0 * * *` (daily at midnight)
- Not 5-field format: `0 0 * * *` ❌

**Issue: Tests fail with "No tests found"**
- Run from project root directory
- Use `-pl` flag for specific module
- Check test class naming matches `*Test.java` pattern

**Issue: MockMvc security errors**
- Import both `SecurityConfig` and `GlobalExceptionHandler`
- Use `.with(user("user").roles("USER"))` in requests
- Include `.with(csrf())` for POST/PUT/DELETE

**Issue: Frontend can't reach API**
- Verify backend is running: `./mvnw spring-boot:run`
- Check CORS configuration in `SecurityConfig`
- Verify proxy configuration in Vite config

**Issue: Exit code 2 from CLI**
- Generic error (config not found, invalid date, etc.)
- Run without `--quiet` to see error message
- Check `schedules.json` format and schedule name

### Debugging Tips

**Enable Debug Logging**:
```yaml
logging:
  level:
    com.jw.holidayguard: DEBUG
    org.springframework.security: DEBUG
```

**Inspect Query Logs** (H2 profile):
```bash
curl -u admin:admin "http://localhost:8080/api/v1/audit-logs/recent?limit=10"
```

**Check Active Version**:
```bash
curl -u user:user "http://localhost:8080/api/v1/schedules/1/versions/active"
```

**Verify Rule Engine Logic**:
- Add breakpoint in `RuleEngineImpl.shouldRun()`
- Check which handler is selected for rule type
- Verify handler's `shouldRun()` logic

**CLI Verbose Mode**:
```bash
java -jar holiday-guard-cli.jar "Schedule Name" --verbose
# Shows rule type, config, and deviation details
```
