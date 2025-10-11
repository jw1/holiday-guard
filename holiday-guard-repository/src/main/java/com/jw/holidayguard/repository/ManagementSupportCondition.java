package com.jw.holidayguard.repository;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Spring {@link Condition} that matches when the active {@link DataProvider}
 * supports management operations.
 *
 * <p>This condition is used by {@link ConditionalOnManagement} to determine
 * whether management controllers and services should be registered in the
 * application context.
 *
 * <p>The condition checks for a {@link DataProvider} bean and calls its
 * {@link DataProvider#supportsManagement()} method. If the method returns
 * {@code true}, the condition matches and beans are registered. If {@code false}
 * or if no DataProvider is found, the condition does not match.
 *
 * <p>Implementation notes:
 * <ul>
 *   <li>The condition is evaluated during bean registration phase</li>
 *   <li>If multiple DataProvider beans exist, Spring will fail (as intended)</li>
 *   <li>If no DataProvider bean exists yet, condition evaluates to false (safe default)</li>
 * </ul>
 */
public class ManagementSupportCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Strategy 1: Check for test system property (set by test configurations)
        String testProperty = System.getProperty("holiday-guard.test.management-enabled");
        System.out.println("[ManagementSupport] Test property value: " + testProperty);

        if ("true".equals(testProperty)) {
            System.out.println("[ManagementSupport] Management operations ENABLED - Test mode");
            return true;
        }

        try {
            // Strategy 2: Try to get bean names for DataProvider type
            String[] beanNames = context.getBeanFactory().getBeanNamesForType(DataProvider.class);
            if (beanNames.length == 0) {
                System.out.println("[ManagementSupport] DataProvider not available, defaulting to DISABLED");
                return false;
            }

            // Strategy 3: Try to get the actual DataProvider bean
            DataProvider dataProvider = context.getBeanFactory().getBean(DataProvider.class);

            // Check if it supports management operations
            boolean supportsManagement = dataProvider.supportsManagement();

            if (supportsManagement) {
                System.out.println("[ManagementSupport] Management operations ENABLED - " + dataProvider.getProviderName());
            } else {
                System.out.println("[ManagementSupport] Management operations DISABLED - " + dataProvider.getProviderName() + " is read-only");
            }

            return supportsManagement;

        } catch (Exception e) {
            // DataProvider bean not available yet (or multiple found) - default to false
            // This is a safe default: management endpoints won't be accidentally exposed
            System.out.println("[ManagementSupport] Exception while checking DataProvider: " + e.getMessage());
            return false;
        }
    }
}
