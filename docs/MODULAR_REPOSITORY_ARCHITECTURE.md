# Modular Repository Architecture

## Overview

Holiday Guard uses a modular architecture with pluggable repository backends and two distinct entrypoints:
1. **Web Server** (holiday-guard-app) - Spring Boot application with REST API and React UI
2. **CLI** (holiday-guard-cli) - Standalone command-line tool with zero Spring dependencies

The architecture allows swapping between different persistence implementations (H2 SQL, JSON files, etc.) without modifying core business logic.

## Complete Module Structure

```
holiday-guard/
├── holiday-guard-domain/                Domain entities (Schedule, Rule, etc.)
├── holiday-guard-repository/            Repository interfaces + DataProvider
│   ├── holiday-guard-repository-h2/     H2/JPA implementation (@Profile("h2"))
│   └── holiday-guard-repository-json/   JSON file implementation (@Profile("json"))
├── holiday-guard-core/                  Business logic (rule engine, services)
├── holiday-guard-security-inmemory/     In-memory authentication (dev/demo)
├── holiday-guard-rest/                  REST API controllers
├── holiday-guard-react/                 React UI (TanStack Query, TypeScript)
├── holiday-guard-app/                   Web Server Entrypoint (Spring Boot)
└── holiday-guard-cli/                   CLI Entrypoint (no Spring, picocli)
```

## Two Entrypoints

### 1. Web Server (holiday-guard-app)

**Purpose**: Full-featured web application with REST API and management UI

**Dependencies**:
```
app → rest → core → repository (interfaces)
       ↓      ↓
     react  repository-h2 OR repository-json
       ↓
   security-inmemory
```

**Features**:
- REST API for schedule queries and management
- React-based management UI
- Spring Security authentication
- H2 or JSON backend (profile-based)
- Port 8080 (default)

**Running**:
```bash
# H2 backend (default)
./mvnw spring-boot:run

# JSON backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=json
```

### 2. CLI (holiday-guard-cli)

**Purpose**: Lightweight command-line tool for schedule queries

**Dependencies**:
```
cli → domain
      ↓
    repository-json (direct file wiring, no Spring)
```

**Features**:
- Zero Spring dependencies (fast startup ~50ms)
- Direct JSON file reading
- Read-only operations
- Manual dependency wiring
- Suitable for shell scripts and cron jobs

**Running**:
```bash
# Query schedule
java -jar holiday-guard-cli.jar query --schedule "Payroll" --date 2025-12-25

# List schedules
java -jar holiday-guard-cli.jar list
```

**Key Design**: CLI manually instantiates rule handlers and repositories to avoid Spring overhead.

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
    boolean supportsManagement();  // Controls CRUD operations
}
```

**Management Support**: The `supportsManagement()` method determines whether schedule management operations (create, update, delete) are available:
- **H2**: Returns `true` - Full CRUD operations enabled
- **JSON**: Returns `false` in Web Server mode - Read-only (configuration managed externally)
- **JSON**: N/A in CLI mode - Direct file writes without Spring

Controllers and services use `@ConditionalOnManagement` to register management endpoints only when supported:

```java
@RestController
@ConditionalOnManagement  // Only registered when DataProvider.supportsManagement() == true
public class ScheduleController {
    // POST, PUT, DELETE endpoints
}
```

### 2. Repository Implementation Modules

**H2 Implementation** (`holiday-guard-repository-h2`):
- `H2DataProvider` (@Profile("h2"))
- `H2RepositoryConfiguration` to enable JPA repositories
- Full CRUD support with SQL database
- Supports management operations (create/update/delete schedules)
- All JPA repositories remain in parent module

**JSON Implementation** (`holiday-guard-repository-json`):
- `JsonDataProvider` (@Profile("json"))
- File-based storage using Jackson ObjectMapper
- Separate JSON files per entity type (schedules.json, rules.json, etc.)
- Read-only in Web Server mode
- Read-write capable in CLI mode (direct file access)
- Ideal for configuration-as-code workflows

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

**application-json.yml**:
```yaml
holiday-guard:
  json:
    schedules-path: ./data/schedules.json
    rules-path: ./data/rules.json
    deviations-path: ./data/deviations.json
    versions-path: ./data/versions.json
server:
  port: 8081  # Different port to allow running both simultaneously
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
# Start with empty database
./mvnw spring-boot:run

# Or start with demo data
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2,demo
```

The application starts with:
- H2 database at `./holiday-guard-data`
- H2 console at http://localhost:8080/h2-console
- Fixed port 8080
- Empty database by default (unless `demo` profile is active)

### Running with JSON Backend

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=json
```

The application starts with:
- JSON files read from `./data/` directory
- Read-only mode (no management endpoints)
- Port 8081 (to allow running alongside H2 instance)

### Running Tests

Tests require an active profile:

```java
@SpringBootTest
@ActiveProfiles("h2")  // Required for DataProvider validation
class MyTest {
    // ...
}
```

## Deployment Scenarios

### Scenario 1: Development and Demo (Web Server + H2)
```bash
# Start with empty database
./mvnw spring-boot:run

# Or start with demo data (4 sample schedules)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2,demo
```
- **Use Case**: Local development, demos, testing
- **Backend**: H2 in-memory or file-based database
- **UI**: React management interface on http://localhost:8080
- **Management**: Full CRUD via UI and API
- **Demo Data**: Optionally activated with `demo` profile (US/UK/Canada/Australia holidays)

### Scenario 2: Production with External Database (Web Server + Postgres)
```bash
./mvnw spring-boot:run -Dspring.profiles.active=postgres
```
- **Use Case**: Production deployment with managed database
- **Backend**: PostgreSQL, MySQL, or other JDBC database
- **UI**: React management interface
- **Management**: Full CRUD via UI and API
- **Requires**: Creating `holiday-guard-repository-postgres` module

### Scenario 3: GitOps / Configuration-as-Code (Web Server + JSON)
```bash
./mvnw spring-boot:run -Dspring.profiles.active=json
```
- **Use Case**: Schedules managed in Git, deployed via CI/CD
- **Backend**: JSON files in `./data/` directory
- **UI**: React read-only dashboard
- **Management**: Edit JSON files directly, version control with Git

### Scenario 4: Shell Script Integration (CLI + JSON)
```bash
java -jar holiday-guard-cli.jar query --schedule "ACH" --date 2025-12-25
```
- **Use Case**: Cron jobs, shell scripts, batch processing
- **Backend**: JSON files read directly
- **UI**: Command-line output
- **Performance**: ~50ms startup time (no Spring overhead)

## Benefits

✅ **Clean Separation**: Core never depends on H2 or JPA directly
✅ **Type-Safe**: Repository interfaces ensure consistency
✅ **Spring Boot Native**: Uses profiles and conditional beans (Web Server)
✅ **Spring-Free Option**: CLI runs without Spring container (fast startup)
✅ **Zero-Config Default**: H2 works immediately after clone
✅ **Simple Switching**: Just change the profile
✅ **Dual Entrypoints**: Web Server for management, CLI for automation
✅ **Extensible**: Add Postgres, MongoDB, S3, etc. by creating new modules
✅ **Fail-Fast**: Missing repository implementation causes clear startup error

## Module Dependencies

### Web Server (holiday-guard-app)

```
app ──→ rest ──→ core ──→ repository (interfaces) ──→ domain
        │        │                │
        │        │                ├─→ repository-h2 (@Profile("h2"))
        │        │                └─→ repository-json (@Profile("json"))
        │        │
        │        └─→ security-inmemory
        │
        └─→ react (React UI, served as static resources)
```

### CLI (holiday-guard-cli)

```
cli ──→ domain
        │
        └─→ repository-json (direct instantiation, no Spring)
```

**Key Differences**:
- **Web Server**: Uses Spring profiles to select repository implementation at runtime
- **CLI**: Directly instantiates JSON repositories without Spring container
- **Web Server**: Includes REST API, React UI, and security layers
- **CLI**: Minimal dependencies for fast startup and shell script integration

## Adding a New Repository Implementation (Web Server)

To add a new backend for the Web Server (e.g., PostgreSQL, MongoDB):

1. Create new module (e.g., `holiday-guard-repository-postgres`)
2. Add dependency on `holiday-guard-repository` (interfaces)
3. Implement `DataProvider` interface with `@Profile("postgres")`:
   ```java
   @Component
   @Profile("postgres")
   public class PostgresDataProvider implements DataProvider {
       public String getProviderName() { return "PostgreSQL"; }
       public String getStorageDescription() { return "PostgreSQL Database"; }
       public boolean supportsManagement() { return true; }  // Full CRUD
   }
   ```
4. Implement all repository interfaces (ScheduleRepository, RuleRepository, etc.)
5. Add module to `holiday-guard-app/pom.xml` dependencies
6. Create `application-postgres.yml` configuration:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/holidayguard
       username: ${DB_USER}
       password: ${DB_PASSWORD}
   ```
7. Run with `-Dspring.profiles.active=postgres`

**Note**: CLI always uses JSON repository and cannot use Spring-based repository implementations.

## Summary

Holiday Guard's modular architecture provides maximum flexibility:

- **10 modules** organized by concern (domain, repository, core, REST, CLI, etc.)
- **2 entrypoints** for different use cases (Web Server vs CLI)
- **3 repository implementations** (H2, JSON with Spring, JSON without Spring)
- **Profile-based configuration** for easy environment switching
- **Management mode control** via DataProvider interface
- **Zero Spring overhead** option for shell script integration

This design enables Holiday Guard to serve both as a full-featured web application and as a lightweight CLI tool, while maintaining clean separation of concerns and extensibility for future backends.