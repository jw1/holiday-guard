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
        if ("true".equals(testProperty)) {
            System.out.println("[ManagementSupport] Management operations ENABLED - Test mode");
            return true;
        }

        // Strategy 2: Check active Spring profiles
        // H2 profile supports management, JSON profile does not
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("h2".equals(profile)) {
                System.out.println("[ManagementSupport] Management operations ENABLED - H2 profile active");
                return true;
            }
            if ("json".equals(profile)) {
                System.out.println("[ManagementSupport] Management operations DISABLED - JSON profile active (read-only)");
                return false;
            }
        }

        // Strategy 3: Try to get the actual DataProvider bean (fallback if profiles not set)
        try {
            String[] beanNames = context.getBeanFactory().getBeanNamesForType(DataProvider.class);
            if (beanNames.length == 0) {
                System.out.println("[ManagementSupport] No profile detected and DataProvider not available yet, defaulting to DISABLED");
                return false;
            }

            DataProvider dataProvider = context.getBeanFactory().getBean(DataProvider.class);
            boolean supportsManagement = dataProvider.supportsManagement();

            if (supportsManagement) {
                System.out.println("[ManagementSupport] Management operations ENABLED - " + dataProvider.getProviderName());
            } else {
                System.out.println("[ManagementSupport] Management operations DISABLED - " + dataProvider.getProviderName() + " is read-only");
            }

            return supportsManagement;

        } catch (Exception e) {
            // DataProvider bean not available yet - default to false
            System.out.println("[ManagementSupport] Exception while checking DataProvider: " + e.getMessage() + ", defaulting to DISABLED");
            return false;
        }
    }
}
