# Schedule Service Design

This document outlines the proposed design for a scheduling service that
supports both evolving business rules and efficient query performance.

------------------------------------------------------------------------

## Core Concepts

### Schedule

-   Represents a **logical schedule** with a stable identifier
    (`schedule_id`).
-   The `schedule_id` never changes, even if the underlying recurrence
    rules or parameters evolve.
-   Business processes can safely reference schedules by their ID
    without worrying about churn.

### Schedule Version

-   Each schedule can have multiple **versions** that capture changes in
    recurrence rules over time.
-   Only one version is **active** at a time.
-   Provides historical traceability for auditing and debugging.

### Materialized Calendar

-   A precomputed table of concrete **occurrences** generated from the
    active schedule version(s).
-   Contains the actual dates/times an event is expected to occur.
-   Regenerated (or incrementally updated) whenever the rules for a
    schedule change.

------------------------------------------------------------------------

## Example Entity Model

### `schedule`

  Column         Type      Notes
  -------------- --------- ------------------------------------
  `id`           UUID      Stable identifier, used by clients
  `name`         String    Human-friendly name
  `created_at`   Instant   Creation timestamp

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
  -----------------------------------------------------------------------

### `schedule_occurrence`

  Column          Type      Notes
  --------------- --------- -----------------------------------
  `id`            UUID      Occurrence ID
  `schedule_id`   UUID      FK to schedule
  `version_id`    UUID      FK to version that generated it
  `occurs_at`     Instant   Concrete date/time
  `status`        Enum      e.g., PENDING, SKIPPED, COMPLETED

------------------------------------------------------------------------

## Query Patterns

-   **Business systems** always call with `schedule_id`.
-   To retrieve upcoming events:
    -   First check `schedule_occurrence` (efficient lookup).
    -   If gaps or inconsistencies are detected, regenerate occurrences
        on the fly from the active rules.
-   The service itself **maintains** the materialized calendar but does
    not strictly **rely** on it for correctness --- it can always fall
    back to rule-based generation.

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

## Future Considerations

-   Incremental refresh of materialized calendar rather than full
    regeneration.
-   Support for exceptions (e.g., holidays, blackout dates).
-   Integration with external workflow engines if required.

------------------------------------------------------------------------

## Summary

This design combines: - **Stable schedule IDs** for business-facing
references. - **Versioning** for rule evolution and auditability. -
**Materialized occurrences** for performance, with graceful fallback to
rule evaluation.

This balances business stability, developer productivity, and
operational performance.
