package com.appointment.frameworks.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Asserts directly on src/main/resources/application.yml rather than booting a Spring context,
 * since src/test/resources/application.yml shadows it entirely on the test classpath and a
 * SpringBootTest here would never see these values.
 */
class ApplicationYamlConfigurationTest {

    private PropertySource<?> loadApplicationYml() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load(
                "application.yml",
                new FileSystemResource(Path.of("src/main/resources/application.yml"))
        );
        return sources.get(0);
    }

    @Test
    void shouldConfigureHikariConnectionPoolWithProductionReadyTimeouts() throws IOException {
        PropertySource<?> properties = loadApplicationYml();

        assertThat(properties.getProperty("spring.datasource.hikari.pool-name")).isEqualTo("AppointmentHikariPool");
        assertThat(properties.getProperty("spring.datasource.hikari.maximum-pool-size")).isEqualTo(10);
        assertThat(properties.getProperty("spring.datasource.hikari.minimum-idle")).isEqualTo(5);
        assertThat(properties.getProperty("spring.datasource.hikari.connection-timeout")).isEqualTo(3000);
        assertThat(properties.getProperty("spring.datasource.hikari.idle-timeout")).isEqualTo(300000);
        assertThat(properties.getProperty("spring.datasource.hikari.max-lifetime")).isEqualTo(900000);
        assertThat(properties.getProperty("spring.datasource.hikari.validation-timeout")).isEqualTo(2000);
        assertThat(properties.getProperty("spring.datasource.hikari.leak-detection-threshold")).isEqualTo(30000);
    }

    @Test
    void shouldConfigureCircuitBreakerWithProductionReadyPolicy() throws IOException {
        PropertySource<?> properties = loadApplicationYml();
        String prefix = "resilience4j.circuitbreaker.instances.appointmentEventProducer.";

        assertThat(properties.getProperty(prefix + "sliding-window-size")).isEqualTo(10);
        assertThat(properties.getProperty(prefix + "minimum-number-of-calls")).isEqualTo(5);
        assertThat(properties.getProperty(prefix + "failure-rate-threshold")).isEqualTo(50);
        assertThat(properties.getProperty(prefix + "slow-call-duration-threshold")).isEqualTo("1s");
        assertThat(properties.getProperty(prefix + "wait-duration-in-open-state")).isEqualTo("5s");
        assertThat(properties.getProperty(prefix + "permitted-number-of-calls-in-half-open-state")).isEqualTo(3);
    }
}
