# Schedule Service Design

This document outlines the proposed design for a scheduling service that
supports both evolving business rules and efficient query performance.

------------------------------------------------------------------------

## Core Concepts

### Primary Use Case: "Should I Run Today?"

This service exists to answer one critical question for daily business processes:
**"Should I run today?"** 

Daily processes (payroll, ACH files, reports, etc.) call a simple REST endpoint to determine if they should execute on a given date, taking into account:
- Base schedule rules (cron-like patterns)
- Holiday calendars 
- One-time exceptions and overrides
- Business day logic

### Schedule

-   Represents a **logical schedule** with a stable identifier
    (`schedule_id`).
-   The `schedule_id` never changes, even if the underlying recurrence
    rules or parameters evolve.
-   Business processes can safely reference schedules by their ID
    without worrying about churn.

### Schedule Version

-   Each schedule can have multiple **versions** that capture changes in
    recurrence rules over time.  This would effectively be a version history.
-   Only the most recent version is **active** at any given time.
-   Provides historical traceability for auditing and debugging.

### Materialized Calendar

-   A precomputed table of concrete **occurrences** generated from the
    active schedule version(s).
-   Contains the actual dates/times an event is expected to occur.
-   Regenerated whenever the rules for a
    schedule change.  We can research incremental changes in v2.

------------------------------------------------------------------------

## Example Entity Model 

### `schedule`

  Column          Type      Notes
  --------------- --------- ------------------------------------
  `id`            UUID      Stable identifier, used by clients (✅ implemented)
  `name`          String    Human-friendly name, unique (✅ implemented)
  `description`   String    Optional description (✅ implemented)
  `country`       String    Country code, defaults to "US" (✅ implemented)
  `active`        Boolean   Schedule enabled/disabled (✅ implemented)
  `created_at`    Instant   Creation timestamp (✅ implemented)
  `updated_at`    Instant   Last modified timestamp (✅ implemented)

**Notes**: This matches our current Schedule entity implementation. The service answers "yes/no" only - business day shifting is a client concern.

### `schedule_version`

  -----------------------------------------------------------------------
  Column                                Type               Notes
  ------------------------------------- ------------------ --------------
  `id`                                  UUID               Version
                                                           identifier

  `schedule_id`                         UUID               FK to schedule

  `rules`                               JSON               Recurrence
                                                           rules (e.g.,
                                                           cron, RRULE)

  `effective_from`                      Instant            When this
                                                           version
                                                           becomes active

  `created_at`                          Instant            Creation
                                                           timestamp

  `is_active`                           Boolean            Whether this
                                                           version is
                                                           currently
                                                           active

Dev note:  Sorting the rules by the "from" date and grabbing the last one presumably gives us the active one... but queries would be easier to write if there is an active flag.
  -----------------------------------------------------------------------

### `schedule_rules`

  Column              Type      Notes
  ------------------- --------- ----------------------------------------
  `id`                UUID      Rule identifier
  `schedule_id`       UUID      FK to schedule
  `rule_type`         Enum      WEEKDAYS_ONLY, CRON_EXPRESSION, etc.
  `rule_config`       JSON      Rule-specific configuration
  `effective_from`    DATE      When this rule becomes active
  `created_at`        Instant   Creation timestamp
  `is_active`         Boolean   Whether this rule is currently active

**Rule Types:**
- `WEEKDAYS_ONLY`: Monday-Friday only
- `CRON_EXPRESSION`: Standard cron pattern
- `CUSTOM_DATES`: Specific date list
- `MONTHLY_PATTERN`: e.g., "first Monday of each month"

**Examples:**
- Weekdays: `rule_type=WEEKDAYS_ONLY, rule_config={}`
- Bi-weekly: `rule_type=CRON_EXPRESSION, rule_config={"cron": "0 0 0 ? * MON/2"}`

### `schedule_occurrence`

  Column          Type      Notes
  --------------- --------- -----------------------------------
  `id`            UUID      Occurrence ID
  `schedule_id`   UUID      FK to schedule
  `version_id`    UUID      FK to version that generated it
  `occurs_on`     DATE      Business date (not time-of-day)
  `status`        Enum      SCHEDULED, OVERRIDDEN, COMPLETED
  `override_id`   UUID      FK to override (if applicable)

**Resolved Dev Note**: Changed from `occurs_at` (Instant) to `occurs_on` (DATE) since we're dealing with business dates only, not specific times. The "should I run today?" question is about dates, not times.

### `schedule_override`

  Column              Type      Notes
  ------------------- --------- ----------------------------------------
  `id`                UUID      Override identifier
  `schedule_id`       UUID      FK to schedule
  `override_date`     DATE      Specific business date to override
  `action`            Enum      SKIP, FORCE_RUN
  `reason`            String    Human-readable explanation
  `created_by`        String    User/system that created override
  `created_at`        Instant   Creation timestamp  
  `expires_at`        DATE      Optional expiration for cleanup

**Override Actions:**
- `SKIP`: Don't run on this date (answer "no")
- `FORCE_RUN`: Run even if base schedule says don't run (answer "yes")

**Examples:**
- Skip payroll on Christmas: `action=SKIP, override_date=2024-12-25, reason="Christmas Day"`
- Force run during maintenance week: `action=FORCE_RUN, override_date=2024-06-15, reason="Catch-up processing"`

**Note**: We only answer "yes" or "no" - rescheduling is a client concern. Client can call the API for different dates to find the next available run date.

### `schedule_occurrence_history`

  Column              Type      Notes
  ------------------- --------- ----------------------------------------
  `id`                UUID      History entry identifier
  `schedule_id`       UUID      FK to schedule
  `query_date`        DATE      Date that was queried
  `should_run_result` Boolean   The answer that was given
  `reason`            String    Explanation for the decision
  `override_applied`  Boolean   Whether an override influenced the result
  `queried_at`        Instant   When the query was made
  `client_identifier` String    Which process/system asked

**Purpose**: Permanent audit trail of what answers were given for historical analysis. Records are never deleted.

------------------------------------------------------------------------

## Core API

### Primary Endpoint: Should I Run Today?

The main API endpoint that daily business processes call:

```
GET /api/v1/schedules/{schedule-id}/should-run?date=2024-12-25
GET /api/v1/schedules/{schedule-id}/should-run  # defaults to today
```

**Response Example:**
```json
{
  "shouldRun": false,
  "reason": "Christmas Day - Federal Holiday",
  "scheduleId": "payroll-biweekly-abc",
  "queryDate": "2024-12-25"
}
```

**Response Fields:**
- `shouldRun`: Boolean indicating if process should execute (yes/no answer only)
- `reason`: Human-readable explanation for the decision
- `scheduleId`: Echo of the requested schedule ID
- `queryDate`: Echo of the query date (useful for debugging)

**Status Codes:**
- `200 OK`: Valid response with should-run decision
- `404 Not Found`: Schedule ID does not exist
- `400 Bad Request`: Invalid date format

**Note**: No `nextRunDate` field - finding next available date is a client concern. Client can query different dates until finding a "yes" answer.

### Override Management APIs

Since there's no UI initially, these APIs are critical for managing schedule exceptions:

#### Create Override
```
POST /api/v1/schedules/{schedule-id}/overrides
{
  "overrideDate": "2024-12-25",
  "action": "SKIP",
  "reason": "Christmas Day",
  "expiresAt": null  # optional, for auto-cleanup
}
```

#### List Overrides
```
GET /api/v1/schedules/{schedule-id}/overrides
GET /api/v1/schedules/{schedule-id}/overrides?from=2024-12-01&to=2024-12-31
```

#### Delete Override
```
DELETE /api/v1/schedules/{schedule-id}/overrides/{override-id}
```

#### Cleanup Expired Overrides
```
DELETE /api/v1/overrides/expired
```

**Note**: Bulk operations removed for v1 - one-at-a-time operations are sufficient initially.

### Preview and Planning APIs

For operational planning without UI:

#### Preview Upcoming Runs
```
GET /api/v1/schedules/{schedule-id}/upcoming?days=30
```

**Response:**
```json
{
  "scheduleId": "payroll-abc",
  "upcoming": [
    {
      "date": "2024-12-02",
      "shouldRun": true,
      "reason": "Scheduled run"
    },
    {
      "date": "2024-12-09", 
      "shouldRun": true,
      "reason": "Scheduled run"
    },
    {
      "date": "2024-12-16",
      "shouldRun": true,
      "reason": "Scheduled run"  
    },
    {
      "date": "2024-12-23",
      "shouldRun": false,
      "reason": "Christmas Week - Override applied"
    }
  ]
}
```

------------------------------------------------------------------------

## Query Patterns

### Primary Use Case: Should-Run Queries

The most common query pattern is daily processes asking "should I run today?":

1. **Query Resolution Order:**
   - Check `schedule_override` table for specific date exceptions
   - If override exists: apply override action (SKIP, FORCE_RUN, RESCHEDULE_TO)
   - If no override: check `schedule_occurrence` for materialized result
   - If materialized result missing: generate on-the-fly from active schedule version rules
   - Apply business day behavior (holiday calendar integration)

2. **Performance Optimization:**
   - Most queries hit `schedule_occurrence` table (pre-materialized)
   - Override checks are fast due to date-based indexing
   - Holiday calendar lookups cached for common dates
   - Bulk queries processed efficiently in single database call

3. **Fallback Strategy:**
   - Service **maintains** materialized calendar but doesn't **rely** on it
   - Can always regenerate occurrences from schedule version rules
   - Ensures reliability even if materialized data is inconsistent

### Materialization Strategy

- **Generate ahead**: Populate `schedule_occurrence` through end of next calendar year
- **Configurable horizon**: Default to next year, customizable via Spring property `schedule.materialization.horizon`
- **Override integration**: Mark occurrences as OVERRIDDEN when exceptions applied  
- **No deletion**: Historical data preserved permanently for audit purposes
- **History tracking**: All queries logged to `schedule_occurrence_history` table

**Use Case**: Viewing schedule patterns for next year is valuable for business planning (e.g., "how many payroll runs in 2025?").

------------------------------------------------------------------------

## Advantages

1.  **Stable References**
    -   Business processes rely on stable `schedule_id`s, avoiding
        breaking changes.
2.  **Historical Context**
    -   Every change to recurrence rules is versioned and auditable.
3.  **Performance**
    -   Materialized calendar allows fast queries for upcoming events,
        without recomputing rules each time.
4.  **Resilience**
    -   If the materialized table is corrupted or lagging, the system
        can regenerate it from source rules.

------------------------------------------------------------------------

## Audit and Monitoring

### Query Logging

All queries are automatically logged to `schedule_occurrence_history` table (see entity model above).

**Key Benefits:**
- **Permanent audit trail**: Never delete historical query results
- **Business analysis**: "What did we tell payroll to do on December 25th?"
- **Compliance**: Complete record of automated decisions
- **Debugging**: Trace why a process did/didn't run on a specific date

### Monitoring Endpoints

For operational visibility:

```
GET /api/v1/admin/query-stats?days=7
GET /api/v1/admin/schedules/{schedule-id}/usage
GET /api/v1/admin/slow-queries?threshold=100ms
```

### Key Metrics to Track

- **Query volume**: Requests per schedule per day
- **Override usage**: How often exceptions are used
- **Performance**: Query response times, cache hit rates
- **Failure patterns**: Which schedules cause issues
- **Business impact**: Skipped runs, rescheduled processes

------------------------------------------------------------------------

## Future Considerations

-   Incremental refresh of materialized calendar rather than full
    regeneration.
-   ~~Support for exceptions (e.g., holidays, blackout dates)~~ ✅ **Completed**: Override system handles this
-   Integration with external workflow engines if required.
-   **Web UI**: Administrative interface for schedule and override management
-   **Advanced notifications**: Proactive alerting for schedule conflicts or issues

------------------------------------------------------------------------

## Summary

This design provides a focused "should I run today?" service that combines:

- **Stable schedule IDs** for business-facing references
- **Versioning** for rule evolution and auditability  
- **Materialized occurrences** for performance, with graceful fallback to rule evaluation
- **Override system** for handling exceptions and one-time adjustments
- **Business day integration** with holiday calendar support
- **Comprehensive APIs** for schedule and override management (no UI required initially)
- **Audit trail** for tracking system usage and business impact

**Core Value Proposition**: Daily business processes get reliable, fast answers to "should I run today?" while operators can easily manage exceptions through simple REST APIs. The system handles the complexity of business rules, holiday calendars, and special cases so individual processes don't have to.

