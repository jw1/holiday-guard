package com.jw.holidayguard.repository;

/**
 * Marker interface to identify which repository implementation is active.
 *
 * <p>Spring Boot application requires exactly one bean implementing this interface
 * to be present in the application context. This ensures that a repository backend
 * (H2, JSON, Postgres, etc.) has been properly configured.
 *
 * <p>Each repository implementation module should provide a @Component bean
 * implementing this interface, annotated with the appropriate @Profile or
 * @Conditional annotation.
 *
 * <p>Example implementations:
 * <ul>
 *   <li>H2DataProvider - SQL/JPA implementation using H2 database (supports management)</li>
 *   <li>JsonDataProvider - File-based JSON implementation (read-only, no management)</li>
 *   <li>PostgresDataProvider - Production SQL implementation using PostgreSQL (supports management)</li>
 * </ul>
 *
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 */
public interface DataProvider {

    /**
     * Returns a human-readable name for this data provider implementation.
     * Used for logging and diagnostics.
     *
     * @return the name of this data provider (e.g., "H2", "JSON", "PostgreSQL")
     */
    String getProviderName();

    /**
     * Returns a description of this data provider's storage mechanism.
     * Used for logging and diagnostics.
     *
     * @return description of where/how data is stored
     */
    String getStorageDescription();

    /**
     * Indicates whether this data provider supports management operations (CRUD).
     *
     * <p>Management operations include:
     * <ul>
     *   <li>Creating, updating, deleting schedules</li>
     *   <li>Creating, updating rules and versions</li>
     *   <li>Creating, updating, deleting deviations (overrides)</li>
     *   <li>Storing query logs for audit trails</li>
     * </ul>
     *
     * <p>SQL-based providers (H2, Postgres) typically return {@code true} because they
     * support full CRUD operations. File-based providers (JSON) typically return {@code false}
     * because they are read-only - data is managed externally (text editor, CI/CD, etc.).
     *
     * <p>When this method returns {@code false}, management endpoints (POST, PUT, DELETE)
     * are automatically disabled, and only query endpoints (GET /should-run, GET /calendar)
     * remain active.
     *
     * @return {@code true} if this provider supports management operations, {@code false} if read-only
     */
    boolean supportsManagement();
}
