package com.appointment.adapters.in.messaging;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.usecases.ports.in.ConfirmAppointmentUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventConsumer.class);

    private final ConfirmAppointmentUseCase confirmAppointmentUseCase;

    public AppointmentEventConsumer(ConfirmAppointmentUseCase confirmAppointmentUseCase) {
        this.confirmAppointmentUseCase = confirmAppointmentUseCase;
    }

    @KafkaListener(
            topics = "${appointment.kafka.topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, AppointmentAvroEvent> record) {
        AppointmentAvroEvent event = record.value();

        log.info("Event received - key: {}, partition: {}, offset: {}, payload: {}",
                record.key(), record.partition(), record.offset(), event);

        confirmAppointmentUseCase.execute(event.getId());
    }
}