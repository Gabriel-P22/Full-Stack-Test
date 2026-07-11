package com.appointment.adapters.in.messaging;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.usecases.ports.in.ConfirmAppointmentUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentEventConsumerTest {

    @Mock
    private ConfirmAppointmentUseCase confirmAppointmentUseCase;

    @InjectMocks
    private AppointmentEventConsumer consumer;

    @Test
    void shouldConfirmAppointmentFromConsumedEvent() {
        UUID id = UUID.randomUUID();
        AppointmentAvroEvent event = AppointmentAvroEvent.newBuilder()
                .setId(id)
                .build();

        ConsumerRecord<String, AppointmentAvroEvent> record =
                new ConsumerRecord<>("appointment-events-topic", 0, 0L, id.toString(), event);

        consumer.consume(record);

        verify(confirmAppointmentUseCase).execute(id);
    }
}
