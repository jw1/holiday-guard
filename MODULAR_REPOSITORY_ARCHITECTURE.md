# Modular Repository Architecture

## Overview

Holiday Guard now supports pluggable repository backends through Spring Boot profiles. The architecture allows swapping between different persistence implementations (H2 SQL, JSON files, etc.) without modifying core business logic.

## Architecture

```
holiday-guard/
├── holiday-guard-domain/          Domain entities (Schedule, Rule, etc.)
├── holiday-guard-repository/      Repository interfaces + DataProvider
├── holiday-guard-repository-h2/   H2/JPA implementation (@Profile("h2"))
├── holiday-guard-core/            Business logic (depends on repository interfaces)
├── holiday-guard-rest/            REST API
├── holiday-guard-app/             Main application (imports repo implementations)
```

## How It Works

### 1. Repository Interfaces (Parent Module)

The `holiday-guard-repository` module defines:
- Repository interfaces (ScheduleRepository, RuleRepository, etc.)
- `DataProvider` marker interface

The `DataProvider` interface ensures exactly one repository implementation is active:

```java
public interface DataProvider {
    String getProviderName();
    String getStorageDescription();
}
```

### 2. H2 Implementation Module

The `holiday-guard-repository-h2` module provides:
- `H2DataProvider` (@Profile("h2"))
- `H2RepositoryConfiguration` to enable JPA repositories
- All existing JPA repositories remain in parent module

### 3. Profile Configuration

**application.yml** (default):
```yaml
spring:
  profiles:
    active: h2  # Default to H2 SQL database
```

**application-h2.yml**:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./holiday-guard-data
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
server:
  port: 8080
```

### 4. Startup Validation

The `RepositoryValidation` configuration in the app module ensures a DataProvider bean exists:

```java
@Configuration
public class RepositoryValidation {
    @Autowired(required = true)
    private DataProvider dataProvider; // Fails if missing or multiple

    @PostConstruct
    public void validate() {
        log.info("Repository Implementation: {}", dataProvider.getProviderName());
    }
}
```

## Usage

### Running with H2 (Default)

```bash
./mvnw spring-boot:run
```

The application starts with:
- H2 database at `./holiday-guard-data`
- H2 console at http://localhost:8080/h2-console
- Fixed port 8080

### Running with Different Profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=json
```

_(JSON implementation not yet created)_

### Running Tests

Tests require an active profile:

```java
@SpringBootTest
@ActiveProfiles("h2")  // Required for DataProvider validation
class MyTest {
    // ...
}
```

## Benefits

✅ **Clean Separation**: Core never depends on H2 or JPA directly
✅ **Type-Safe**: Repository interfaces ensure consistency
✅ **Spring Boot Native**: Uses profiles and conditional beans
✅ **Zero-Config Default**: H2 works immediately after clone
✅ **Simple Switching**: Just change the profile
✅ **Extensible**: Add Postgres, MongoDB, S3, etc. by creating new modules
✅ **Fail-Fast**: Missing repository implementation causes clear startup error

## Module Dependencies

```
app → rest → core → repository (interfaces)
              ↓
         repository-h2 (impl)
```

The app module imports `repository-h2` which provides the H2 implementation.

## Adding a New Repository Implementation

1. Create new module (e.g., `holiday-guard-repository-postgres`)
2. Implement `DataProvider` with `@Profile("postgres")`
3. Implement all repository interfaces
4. Add module to app dependencies
5. Create `application-postgres.yml` configuration
6. Run with `-Dspring.profiles.active=postgres`

## Files Modified

### Created