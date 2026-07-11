package com.appointment.adapters.out.messaging;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.adapters.out.messaging.dto.AppointmentStatusAvro;
import com.appointment.entities.Appointment;
import com.appointment.frameworks.config.AppointmentKafkaProperties;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.ZoneOffset;

@Component
public class AppointmentEventProducerImpl implements AppointmentEventProducer {

    private final KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate;
    private final AppointmentKafkaProperties kafkaProperties;

    public AppointmentEventProducerImpl(
            KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate,
            AppointmentKafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

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
}