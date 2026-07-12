package com.appointment.adapters.out.messaging;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.adapters.out.messaging.dto.AppointmentStatusAvro;
import com.appointment.entities.Appointment;
import com.appointment.frameworks.config.AppointmentKafkaProperties;
import com.appointment.frameworks.exceptions.AppointmentEventPublishingException;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.ZoneOffset;

@Component
public class AppointmentEventProducerImpl implements AppointmentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventProducerImpl.class);
    private static final String CIRCUIT_BREAKER_NAME = "appointmentEventProducer";

    private final KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate;
    private final AppointmentKafkaProperties kafkaProperties;

    public AppointmentEventProducerImpl(
            KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate,
            AppointmentKafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "publishFallback")
    public Appointment execute(final Appointment appointment) {
        AppointmentAvroEvent avroEvent = AppointmentAvroEvent.newBuilder()
                .setId(appointment.id())
                .setPatientCpf(appointment.patientCpf())
                .setPatientName(appointment.patientName())
                .setScheduledAt(appointment.scheduledAt().toInstant(ZoneOffset.UTC))
                .setCreatedAt(appointment.createdAt().toInstant(ZoneOffset.UTC))
                .setUpdatedAt(appointment.updatedAt() != null ?
                        appointment.updatedAt().toInstant(ZoneOffset.UTC) : null)
                .setObservation(appointment.observation().orElse(null))
                .setStatus(AppointmentStatusAvro.valueOf(appointment.status().name()))
                .build();

        kafkaTemplate.send(kafkaProperties.getTopic(), appointment.id().toString(), avroEvent);

        return appointment;
    }

    private Appointment publishFallback(final Appointment appointment, final Throwable throwable) {
        log.error("Circuit breaker '{}' triggered fallback while publishing appointment {}",
                CIRCUIT_BREAKER_NAME, appointment.id(), throwable);
        throw new AppointmentEventPublishingException(
                "Unable to publish appointment event for id " + appointment.id(), throwable);
    }
}
