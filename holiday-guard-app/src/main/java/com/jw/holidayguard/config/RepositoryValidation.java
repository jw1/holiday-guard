package com.jw.holidayguard.config;

import com.jw.holidayguard.repository.DataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Validation configuration to ensure exactly one repository implementation is active.
 *
 * <p>This configuration requires a {@link DataProvider} bean to be present in the
 * application context. If no repository implementation is configured (missing profile),
 * or if multiple implementations are accidentally active, Spring will fail to start
 * with a clear error message.
 *
 * <p>The DataProvider bean is provided by repository implementation modules:
 * <ul>
 *   <li>holiday-guard-repository-h2 (H2DataProvider with @Profile("h2"))</li>
 *   <li>holiday-guard-repository-json (JsonDataProvider with @Profile("json"))</li>
 * </ul>
 */
@Slf4j
@Configuration
public class RepositoryValidation {

    private final DataProvider dataProvider;

    /**
     * Constructor injection ensures Spring fails fast if no DataProvider is available.
     *
     * @param dataProvider the active repository implementation
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if no DataProvider bean exists
     * @throws org.springframework.beans.factory.NoUniqueBeanDefinitionException if multiple DataProvider beans exist
     */
    @Autowired
    public RepositoryValidation(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @PostConstruct
    public void validateAndLog() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Repository Implementation: {}", dataProvider.getProviderName());
        log.info("  Storage: {}", dataProvider.getStorageDescription());
        log.info("  Management Operations: {}", dataProvider.supportsManagement() ? "ENABLED (CRUD)" : "DISABLED (Read-Only)");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
