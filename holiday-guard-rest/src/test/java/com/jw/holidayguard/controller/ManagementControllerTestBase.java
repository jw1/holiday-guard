package com.jw.holidayguard.controller;

/**
 * Base class for management controller tests.
 *
 * <p>Sets up the test environment to enable management operations by setting
 * a system property that the ManagementSupportCondition checks. This allows
 * @ConditionalOnManagement controllers to be registered in test contexts.
 *
 * <p>The static initializer runs when the class is loaded, ensuring the
 * system property is set before Spring evaluates the @ConditionalOnManagement
 * condition.
 */
public abstract class ManagementControllerTestBase {

    static {
        // Set system property before Spring context initialization
        System.setProperty("holiday-guard.test.management-enabled", "true");
    }
}
