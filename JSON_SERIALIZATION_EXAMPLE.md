# Calendar JSON Serialization

The `Calendar` class supports JSON serialization and deserialization, enabling use cases like:
- File-based configuration (JSON repository)
- Command-line applications that read schedule config from files
- API responses and caching
- Exporting/importing calendar configurations

## Overview

The Calendar class has two static methods:
- `toJson()` - Serializes a Calendar to JSON string
- `fromJson(String json, RuleEvaluator evaluator)` - Deserializes JSON to Calendar

## Design Notes

### Why RuleEvaluator is Not Serialized

The `RuleEvaluator` is a **strategy** (behavior), not **data**. It's marked with `@JsonIgnore` because:
- It represents executable logic, not state
- Different deployment contexts may use different evaluator implementations
- Serializing lambdas/function references is problematic and non-portable

When deserializing, you must provide a `RuleEvaluator` implementation. This is typically the `RuleEngine` from the core module.

## Example: Round-Trip Serialization

```java
// 1. Create a Calendar with schedule, rule, and deviations
Schedule schedule = Schedule.builder()
    .id(1L)
    .name("Payroll Schedule")
    .description("Weekday payroll processing")
    .build();

Rule weekdaysRule = Rule.builder()
    .id(10L)
    .scheduleId(1L)
    .versionId(100L)
    .ruleType(Rule.RuleType.WEEKDAYS_ONLY)
    .build();

Deviation skipDeviation = Deviation.builder()
    .id(5L)
    .scheduleId(1L)
    .versionId(100L)
    .overrideDate(LocalDate.of(2025, 1, 6))
    .action(RunStatus.FORCE_SKIP)
    .reason("Holiday")
    .build();

RuleEvaluator ruleEvaluator = new RuleEngine(handlers); // Your rule evaluator

Calendar calendar = new Calendar(
    schedule,
    weekdaysRule,
    List.of(skipDeviation),
    ruleEvaluator
);

// 2. Serialize to JSON
String json = calendar.toJson();
System.out.println(json);

// 3. Deserialize from JSON (must provide RuleEvaluator)
Calendar reconstructed = Calendar.fromJson(json, ruleEvaluator);

// 4. Use the reconstructed calendar
boolean shouldRun = reconstructed.shouldRun(LocalDate.of(2025, 1, 6));
// returns false due to FORCE_SKIP deviation
```

## Example JSON Output

```json
{
  "schedule": {
    "id": 1,
    "name": "Payroll Schedule",
    "description": "Weekday payroll processing",
    "country": null,
    "active": true,
    "createdAt": null,
    "createdBy": null,
    "updatedAt": null,
    "updatedBy": null
  },
  "rule": {
    "id": 10,
    "scheduleId": 1,
    "versionId": 100,
    "ruleType": "WEEKDAYS_ONLY",
    "ruleConfig": null,
    "active": true,
    "effectiveFrom": null,
    "effectiveTo": null,
    "createdAt": null
  },
  "deviations": [
    {
      "id": 5,
      "scheduleId": 1,
      "versionId": 100,
      "overrideDate": "2025-01-06",
      "action": "FORCE_SKIP",
      "reason": "Holiday",
      "createdBy": null,
      "createdAt": null,
      "expiresAt": null
    }
  ]
}
```

## Use Case: Command-Line Application

```java
public class ScheduleChecker {
    public static void main(String[] args) throws IOException {
        // 1. Read calendar configuration from file
        String json = Files.readString(Path.of("payroll-schedule.json"));

        // 2. Create rule evaluator (would use real RuleEngine in practice)
        RuleEvaluator evaluator = new RuleEngine(handlers);

        // 3. Deserialize calendar
        Calendar calendar = Calendar.fromJson(json, evaluator);

        // 4. Check if should run today
        LocalDate today = LocalDate.now();
        boolean shouldRun = calendar.shouldRun(today);

        System.out.println("Should run today (" + today + ")? " + shouldRun);
        System.exit(shouldRun ? 0 : 1);
    }
}
```

## Use Case: JSON-Based Repository

```java
public class JsonCalendarRepository {
    private final Path storageDir;
    private final RuleEvaluator ruleEvaluator;

    public JsonCalendarRepository(Path storageDir, RuleEvaluator ruleEvaluator) {
        this.storageDir = storageDir;
        this.ruleEvaluator = ruleEvaluator;
    }

    public void save(Long scheduleId, Calendar calendar) throws IOException {
        String json = calendar.toJson();
        Path file = storageDir.resolve(scheduleId + ".json");
        Files.writeString(file, json);
    }

    public Calendar load(Long scheduleId) throws IOException {
        Path file = storageDir.resolve(scheduleId + ".json");
        String json = Files.readString(file);
        return Calendar.fromJson(json, ruleEvaluator);
    }

    public List<Calendar> loadAll() throws IOException {
        return Files.list(storageDir)
            .filter(p -> p.toString().endsWith(".json"))
            .map(p -> {
                try {
                    String json = Files.readString(p);
                    return Calendar.fromJson(json, ruleEvaluator);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .collect(Collectors.toList());
    }
}
```

## Tests

The JSON serialization functionality is thoroughly tested in `CalendarTest.java`:

- `toJsonSerializesCompleteState()` - Verifies all fields are serialized
- `fromJsonReconstructsCalendar()` - Verifies deserialization reconstructs state
- `roundTripJsonPreservesCalendarBehavior()` - Verifies round-trip preserves behavior

All 11 CalendarTest tests pass, including the 3 JSON-related tests.

## Implementation Details

The implementation uses Jackson's `ObjectMapper` with the `JavaTimeModule` for proper `LocalDate` serialization:

```java
private static final ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule());
```

A private `CalendarData` DTO class is used for deserialization since the `RuleEvaluator` field is marked `@JsonIgnore` and must be provided separately.
