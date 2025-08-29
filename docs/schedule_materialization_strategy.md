# Schedule Materialization Strategy

This document outlines the implementation strategy for materializing schedule calendars and handling version changes. This represents future work that will be tackled in separate feature branches.

------------------------------------------------------------------------

## Core Materialization Process

### **When Materialization Occurs**

1. **Version Changes**: Spring Boot application event triggered whenever schedule rules or overrides change
2. **Startup Verification**: On application startup, reconstruct calendars and verify against existing materialized data
3. **Manual Trigger**: Exposed REST endpoint for manual re-materialization (ops/debugging)
4. **Scheduled Refresh**: Extend materialization horizon as time progresses

### **Event-Driven Re-materialization**

**Trigger Events:**
- New `schedule_version` created
- `schedule_rules` added/modified for active version  
- `schedule_override` added/modified/deleted
- Schedule activation/deactivation

**Event Flow:**
```java
@EventListener
public void handleScheduleChanged(ScheduleVersionChangedEvent event) {
    materializationService.rematerialize(event.getScheduleId(), event.getVersionId());
}
```

**Event Payload Ideas:**
- `schedule_id`: Which schedule changed
- `version_id`: New/changed version
- `change_type`: RULES_CHANGED, OVERRIDE_ADDED, etc.
- `effective_date`: When changes take effect

------------------------------------------------------------------------

## Implementation Gaps & Future Work

### **Materialization Engine**

**Components Needed:**
- `ScheduleMaterializationService`: Core calendar generation logic
- `RuleEngine`: Processes different rule types (WEEKDAYS_ONLY, CRON_EXPRESSION, etc.)
- `OverrideApplicator`: Applies SKIP/FORCE_RUN modifications to base calendar
- `MaterializationEventPublisher`: Triggers re-materialization on changes

**Rule Type Handlers:**
- `WeekdaysOnlyHandler`: Monday-Friday generation
- `CronExpressionHandler`: Parse and evaluate cron patterns
- `CustomDatesHandler`: Handle specific date lists
- `MonthlyPatternHandler`: "First Monday of each month" logic

### **Version Lifecycle Management**

**Open Questions:**
- **Transition handling**: How to handle dates that fall between version effective dates
- **Retroactive changes**: What happens when historical rules are modified
- **Partial materialization**: Can we materialize just changed date ranges vs full calendar
- **Conflict resolution**: Multiple overrides for same date/version
- **Performance optimization**: Incremental updates vs full regeneration

### **Startup Verification Process**

**Algorithm:**
1. For each active schedule:
   - Reconstruct materialized calendar from current version + overrides
   - Compare against existing `schedule_materialized_calendar` data
   - Log discrepancies 
   - Option to auto-fix vs manual intervention

**Edge Cases:**
- Clock skew between app instances
- Partial materialization failures
- Database inconsistencies
- Missing version data

------------------------------------------------------------------------

## Data Consistency & Integrity

### **Atomic Operations**

**Challenge**: Ensure materialized calendar stays consistent with rules/overrides

**Solutions:**
- Database transactions across all related tables
- Optimistic locking on schedule versions
- Event ordering guarantees
- Rollback strategies for failed materialization

### **Historical Reconstruction**

**Use Case**: "What would the calendar have looked like on date X with version Y?"

**Requirements:**
- Reconstruct any historical calendar state
- Account for overrides that were active at that time
- Handle expired overrides correctly
- Support audit queries: "Why didn't we run on 2024-03-15?"

**Implementation Ideas:**
- Time-travel queries using version effective dates
- Override validity period calculations
- Cached historical materializations for performance

------------------------------------------------------------------------

## Performance Considerations

### **Scalability Concerns**

**Volume Estimates:**
- 1000+ schedules × 365+ days/year = 365K+ materialized records per year
- High query volume during business hours
- Bulk materialization after major rule changes

**Optimization Strategies:**
- Indexed queries by schedule_id + occurs_on
- Partitioning by date ranges
- Read replicas for query load
- Caching for frequently accessed schedules

### **Materialization Horizon Management**

**Current Strategy**: Materialize through end of next calendar year

**Future Enhancements:**
- Dynamic horizon based on schedule usage patterns
- Lazy materialization for rarely-queried future dates
- Background extension of horizon as time progresses
- Configurable horizons per schedule type

------------------------------------------------------------------------

## Operational Features

### **Manual Re-materialization API**

```
POST /api/v1/admin/schedules/{schedule-id}/rematerialize
POST /api/v1/admin/schedules/rematerialize-all
GET /api/v1/admin/schedules/{schedule-id}/materialization-status
```

**Use Cases:**
- Fix data inconsistencies
- Test rule changes before deployment
- Recover from materialization failures
- Bulk updates during maintenance windows

### **Monitoring & Alerting**

**Metrics to Track:**
- Materialization execution time per schedule
- Calendar size growth over time
- Query performance against materialized data
- Event processing lag
- Failed materializations

**Alert Conditions:**
- Materialization failures
- Calendar size exceeding thresholds  
- Query performance degradation
- Event processing backlog

------------------------------------------------------------------------

## Testing Strategy

### **Unit Testing**

**Rule Engine Tests:**
- Each rule type handler with various configurations
- Override application logic (SKIP/FORCE_RUN)
- Edge cases: leap years, DST changes, holiday overlaps
- Boundary conditions: year transitions, effective dates

### **Integration Testing**

**Event Flow Tests:**
- Version change triggers re-materialization
- Materialized data matches rule + override expectations  
- Query responses reflect materialized calendar
- Audit trail captures all interactions

**Performance Tests:**
- Large schedule materialization (10K+ dates)
- Concurrent materialization requests
- High query volume against materialized data
- Memory usage during bulk operations

------------------------------------------------------------------------

## Future Enhancements

### **Advanced Rule Types**

**Candidates for Future Rule Types:**
- `BUSINESS_DAYS_ONLY`: Weekdays excluding holidays
- `NTH_WEEKDAY_OF_MONTH`: "2nd Friday of each month"
- `QUARTERLY_PATTERN`: End-of-quarter processing
- `FISCAL_CALENDAR`: Non-standard year boundaries
- `CONDITIONAL_RULES`: "Run if previous day was holiday"

### **Smart Materialization**

**Ideas:**
- Machine learning to predict optimal materialization horizons
- Differential materialization (only changed date ranges)  
- Predictive caching based on query patterns
- Automatic rule optimization suggestions

### **Integration Features**

**External System Integration:**
- Import holidays from external calendar services
- Sync with HR systems for company-specific dates
- Integration with workflow engines for downstream triggering
- Webhook notifications for schedule changes

------------------------------------------------------------------------

## Migration & Deployment

### **Database Migration Strategy**

**Phase 1**: Core tables and basic materialization
**Phase 2**: Event-driven re-materialization  
**Phase 3**: Advanced rule types and optimization
**Phase 4**: Operational features and monitoring

**Migration Considerations:**
- Zero-downtime deployments
- Backward compatibility during transitions
- Data migration from existing systems
- Rollback procedures for each phase

### **Feature Flag Strategy**

**Flags for Gradual Rollout:**
- `materialization.event-driven.enabled`
- `materialization.startup-verification.enabled` 
- `materialization.auto-fix-discrepancies.enabled`
- `admin.manual-rematerialization.enabled`

This allows safe deployment and rollback of materialization features independently.