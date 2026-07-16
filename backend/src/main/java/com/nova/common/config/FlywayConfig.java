package com.nova.common.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires Flyway in place of Spring Boot's {@code FlywayAutoConfiguration.FlywayConfiguration}.
 *
 * <p>Spring Boot 3.5.5's auto-configured {@code FlywayConfiguration} calls
 * {@code FluentConfiguration.cleanOnValidationError(boolean)}. That method was
 * removed in Flyway 12, so Boot's auto-config fails to start on Flyway 12.x.
 * This project pins Flyway 12.x deliberately: the final Flyway 11.x does not
 * recognise PostgreSQL 18, whereas the running database is PostgreSQL 18.4.
 *
 * <p>Boot's {@code FlywayConfiguration} is itself {@code @ConditionalOnMissingBean(Flyway.class)},
 * so supplying our own {@code Flyway} bean disables it entirely — including the
 * {@code FlywayMigrationInitializer} that actually runs the migrations. We
 * therefore re-create both: the {@code Flyway} bean (built without the removed
 * method) and the {@code flywayInitializer} bean (named exactly so Boot's
 * dependency configurer makes the JPA {@code EntityManagerFactory} wait for the
 * schema to be migrated before Hibernate validates it).
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    private final FlywayProperties properties;
    private final DataSource dataSource;

    public FlywayConfig(FlywayProperties properties, DataSource dataSource) {
        this.properties = properties;
        this.dataSource = dataSource;
    }

    @Bean
    @ConditionalOnMissingBean(Flyway.class)
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(properties.getLocations().toArray(new String[0]))
                .baselineOnMigrate(properties.isBaselineOnMigrate())
                .baselineVersion(properties.getBaselineVersion())
                .load();
    }

    @Bean
    @ConditionalOnMissingBean(FlywayMigrationInitializer.class)
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}
