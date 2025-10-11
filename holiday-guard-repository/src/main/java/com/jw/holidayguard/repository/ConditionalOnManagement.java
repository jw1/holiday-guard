package com.jw.holidayguard.repository;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Conditional annotation that activates beans only when the active DataProvider
 * supports management operations (CRUD).
 *
 * <p>This annotation is used to conditionally enable management controllers,
 * services, and other components that require full CRUD capabilities. When a
 * read-only repository implementation (like JSON) is active, these components
 * are automatically disabled.
 *
 * <p>Example usage:
 * <pre>
 * &#64;RestController
 * &#64;ConditionalOnManagement
 * public class ScheduleController {
 *     // POST, PUT, DELETE endpoints only active with H2/SQL
 * }
 * </pre>
 *
 * <p>The condition checks the {@link DataProvider#supportsManagement()} method
 * of the active data provider bean. If it returns {@code true}, beans annotated
 * with this annotation are registered. If {@code false}, they are skipped.
 *
 * @see DataProvider#supportsManagement()
 * @see ManagementSupportCondition
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ManagementSupportCondition.class)
public @interface ConditionalOnManagement {
}
