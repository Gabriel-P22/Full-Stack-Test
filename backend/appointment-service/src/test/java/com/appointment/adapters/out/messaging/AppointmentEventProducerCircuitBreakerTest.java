package com.appointment.adapters.out.messaging;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.frameworks.exceptions.AppointmentEventPublishingException;
import com.appointment.frameworks.spring.AppointmentApplication;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Exercises the real Resilience4j-backed circuit breaker around {@link AppointmentEventProducerImpl},
 * relying on Spring AOP proxying rather than a plain unit test, since the {@code @CircuitBreaker}
 * annotation only takes effect on a Spring-managed bean. The policy is set inline via
 * {@code properties} (mirroring application.yml) because src/test/resources/application.yml
 * shadows the main config entirely on the test classpath.
 */
@SpringBootTest(classes = AppointmentApplication.class, properties = {
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.sliding-window-type=COUNT_BASED",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.sliding-window-size=10",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.slow-call-duration-threshold=1s",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.slow-call-rate-threshold=100",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.wait-duration-in-open-state=5s",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.permitted-number-of-calls-in-half-open-state=3",
        "resilience4j.circuitbreaker.instances.appointmentEventProducer.automatic-transition-from-open-to-half-open-enabled=true"
})
class AppointmentEventProducerCircuitBreakerTest {

    private static final String CIRCUIT_BREAKER_NAME = "appointmentEventProducer";

    @MockitoBean
    private KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate;

    @Autowired
    private AppointmentEventProducer producer;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        circuitBreaker.reset();
    }

    private Appointment appointment() {
        return new Appointment(
                UUID.randomUUID(),
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    @Test
    void shouldExposeConfiguredCircuitBreakerPolicy() {
        var config = circuitBreaker.getCircuitBreakerConfig();

        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(config.getFailureRateThreshold()).isEqualTo(50.0f);
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
    }

    @Test
    void shouldPublishSuccessfullyWhileCircuitIsClosed() {
        Appointment appointment = appointment();

        Appointment result = producer.execute(appointment);

        assertThat(result).isEqualTo(appointment);
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void shouldOpenCircuitAfterFailureRateThresholdIsExceeded() {
        doThrow(new RuntimeException("kafka unavailable"))
                .when(kafkaTemplate).send(anyString(), anyString(), any());

        for (int i = 0; i < 5; i++) {
            Appointment appointment = appointment();
            assertThatThrownBy(() -> producer.execute(appointment))
                    .isInstanceOf(AppointmentEventPublishingException.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldShortCircuitAndSkipKafkaCallWhenCircuitIsOpen() {
        doThrow(new RuntimeException("kafka unavailable"))
                .when(kafkaTemplate).send(anyString(), anyString(), any());

        for (int i = 0; i < 5; i++) {
            Appointment appointment = appointment();
            assertThatThrownBy(() -> producer.execute(appointment))
                    .isInstanceOf(AppointmentEventPublishingException.class);
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Appointment appointmentWhileOpen = appointment();
        assertThatThrownBy(() -> producer.execute(appointmentWhileOpen))
                .isInstanceOf(AppointmentEventPublishingException.class);

        verify(kafkaTemplate, times(5)).send(anyString(), anyString(), any());
    }

    @Test
    void shouldAllowTestCallsWhenTransitioningToHalfOpen() {
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();

        Appointment appointment = appointment();
        Appointment result = producer.execute(appointment);

        assertThat(result).isEqualTo(appointment);
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }
}
