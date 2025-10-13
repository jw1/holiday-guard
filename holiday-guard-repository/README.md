# Holiday Guard Repository Module

This module defines the repository interfaces and provides pluggable data access implementations. Holiday Guard supports multiple storage backends, selected via Spring profiles.

## Repository Architecture

### Core Interfaces

All repository interfaces extend Spring Data JPA's `JpaRepository` and define standard CRUD operations:

- **ScheduleRepository** - Schedule entity persistence
- **VersionRepository** - Version history tracking
- **RuleRepository** - Rule configuration storage
- **DeviationRepository** - Deviation (override) management
- **QueryLogRepository** - Audit trail of shouldRun queries (H2 only)

### DataProvider Interface

The `DataProvider` interface allows different storage backends to declare their capabilities:

```java
public interface DataProvider {
    String getProviderName();           // e.g., "H2", "JSON"
    String getStorageDescription();     // Human-readable description
    boolean supportsManagement();       // Whether CRUD operations are supported
}
```

The `supportsManagement()` method determines whether management controllers (create/update/delete) are enabled.

## Storage Implementations

### H2 Profile (SQL Database)

**Activation:** `spring.profiles.active=h2`

**Module:** `holiday-guard-repository-h2`

**Features:**
- ✅ Full CRUD operations
- ✅ Management UI enabled
- ✅ Query logging and audit trails
- ✅ JPA/Hibernate persistence
- ✅ Transaction support
- ✅ In-memory database (development/testing)

**Configuration:**
```yaml
spring:
  profiles:
    active: h2
  datasource:
    url: jdbc:h2:mem:holidayguard
  jpa:
    hibernate:
      ddl-auto: create-drop
```

**Best for:**
- Development and testing
- Production deployments requiring CRUD
- Environments needing audit trails
- Multi-user scenarios with concurrent access

### JSON Profile (File-Based)

**Activation:** `spring.profiles.active=json`

**Module:** `holiday-guard-repository-json`

**Features:**
- ✅ Read-only operations
- ❌ Management UI disabled (404 responses)
- ❌ No audit logging
- ✅ Simple file-based storage
- ✅ Fast startup
- ✅ Version control friendly (data.json)

**Configuration:**
```yaml
spring:
  profiles:
    active: json
app:
  repo:
    json:
      filename: ./data.json
```

**Best for:**
- Simple deployments
- CI/CD environments
- Embedded use cases
- Read-only schedule queries
- Version-controlled schedule definitions

**Limitations:**
- All write operations throw `UnsupportedOperationException`
- No QueryLog support
- Changes require application restart

## Conditional Features

### @ConditionalOnManagement

This custom Spring annotation conditionally enables controllers and services based on `DataProvider.supportsManagement()`:

```java
@ConditionalOnManagement
@RestController
public class ScheduleController {
    // Only registered when DataProvider.supportsManagement() == true
}
```

**Enabled for:** H2 profile
**Disabled for:** JSON profile

When disabled, management endpoints return 404 instead of being registered in the application context.

### ManagementSupportCondition

The implementation class for `@ConditionalOnManagement`. It evaluates the condition during Spring context initialization:

```java
public class ManagementSupportCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Check if DataProvider bean supports management
        DataProvider provider = context.getBeanFactory().getBean(DataProvider.class);
        return provider.supportsManagement();
    }
}
```

## Query Methods

Beyond standard JPA methods, repositories define custom queries:

### ScheduleRepository
```java
Optional<Schedule> findByName(String name);
List<Schedule> findByActiveTrue();
boolean existsByNameIgnoreCase(String name);
```

### VersionRepository
```java
Optional<Version> findByScheduleIdAndActiveTrue(Long scheduleId);
List<Version> findByScheduleIdOrderByVersionNumberDesc(Long scheduleId);
```

### RuleRepository
```java
Optional<Rule> findByVersionId(Long versionId);
```

### DeviationRepository
```java
List<Deviation> findByVersionId(Long versionId);
List<Deviation> findByScheduleIdAndVersionId(Long scheduleId, Long versionId);
Optional<Deviation> findByScheduleIdAndDeviationDate(Long scheduleId, LocalDate date);
```

### QueryLogRepository (H2 only)
```java
List<QueryLog> findByScheduleIdOrderByQueryTimestampDesc(Long scheduleId);
List<QueryLog> findTop100ByOrderByQueryTimestampDesc();
```

## Module Structure

```
holiday-guard-repository/          # Core interfaces
├── DataProvider.java
├── ScheduleRepository.java
├── VersionRepository.java
├── RuleRepository.java
├── DeviationRepository.java
├── QueryLogRepository.java
├── @ConditionalOnManagement
└── ManagementSupportCondition

holiday-guard-repository-h2/       # H2 implementation
└── H2DataProvider.java

holiday-guard-repository-json/     # JSON implementation
└── JsonDataProvider.java
```

## Switching Between Profiles

### Development
```bash
# H2 with management UI
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# JSON read-only mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=json
```

### Production
```yaml
# application-prod.yml
spring:
  profiles:
    active: h2  # or json
```

## Testing

Repository tests use `@DataJpaTest` for isolated database testing:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ScheduleRepositoryTest {
    @Autowired
    private ScheduleRepository repository;

    @Test
    void shouldFindScheduleByName() {
        // Test implementation
    }
}
```

## Transaction Management

H2 repositories support full Spring transaction management:

```java
@Transactional
public Version createNewVersion(Schedule schedule, CreateRuleRequest ruleRequest) {
    // Multiple repository operations in a single transaction
    Version version = versionRepository.save(...);
    ruleRepository.save(...);
    return version;
}
```

JSON repositories ignore `@Transactional` annotations since all operations are read-only.

## Key Design Principles

1. **Profile-Based Selection** - Storage backend chosen at startup via Spring profiles
2. **Capability Declaration** - `DataProvider.supportsManagement()` controls feature availability
3. **Conditional Registration** - Controllers/services only registered when supported
4. **Graceful Degradation** - Management endpoints return 404 for read-only backends
5. **Interface Consistency** - Same repository interfaces across all implementations
