package com.appointment.adapters.out.messaging;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.frameworks.config.AppointmentKafkaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentEventProducerImplTest {

    @Mock
    private KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate;

    @Mock
    private AppointmentKafkaProperties kafkaProperties;

    @InjectMocks
    private AppointmentEventProducerImpl producer;

    @BeforeEach
    void setUp() {
        when(kafkaProperties.getTopic()).thenReturn("appointment-events-topic");
    }

    @Test
    void shouldPublishAvroEventAndReturnTheSameAppointment() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment(
                id,
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                null
        );

        Appointment result = producer.execute(appointment);

        assertThat(result).isEqualTo(appointment);

        ArgumentCaptor<AppointmentAvroEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentAvroEvent.class);
        verify(kafkaTemplate).send(eq("appointment-events-topic"), eq(id.toString()), eventCaptor.capture());

        AppointmentAvroEvent event = eventCaptor.getValue();
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getPatientCpf()).isEqualTo("52998224725");
        assertThat(event.getUpdatedAt()).isNull();
    }

    @Test
    void shouldIncludeObservationAndUpdatedAtWhenPresent() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment(
                id,
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                Optional.of("some observation"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        producer.execute(appointment);

        ArgumentCaptor<AppointmentAvroEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentAvroEvent.class);
        verify(kafkaTemplate).send(eq("appointment-events-topic"), eq(id.toString()), eventCaptor.capture());

        AppointmentAvroEvent event = eventCaptor.getValue();
        assertThat(event.getObservation()).isEqualTo("some observation");
        assertThat(event.getUpdatedAt()).isNotNull();
    }
}
