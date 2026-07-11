package com.appointment.frameworks.config;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.usecases.ports.in.CancelAppointmentDueToProcessingFailureUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final long RETRY_INTERVAL_MS = 1_000L;
    private static final long RETRY_MAX_ATTEMPTS = 2L;

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, this::resolveDeadLetterTopic);
    }

    private TopicPartition resolveDeadLetterTopic(ConsumerRecord<?, ?> record, Exception exception) {
        String suffix = isDeserializationFailure(exception) ? "-deserialization-dlt" : "-dlt";
        return new TopicPartition(record.topic() + suffix, -1);
    }

    private boolean isDeserializationFailure(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof DeserializationException) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
            CancelAppointmentDueToProcessingFailureUseCase cancelAppointmentUseCase
    ) {
        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            if (record.value() instanceof AppointmentAvroEvent event) {
                cancelAppointmentUseCase.execute(event.getId());
            }

            deadLetterPublishingRecoverer.accept(record, exception);
        };

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(RETRY_INTERVAL_MS, RETRY_MAX_ATTEMPTS)
        );

        errorHandler.addNotRetryableExceptions(DeserializationException.class);

        return errorHandler;
    }
}
