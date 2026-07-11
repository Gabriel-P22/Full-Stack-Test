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

    private final AppointmentKafkaProperties kafkaProperties;

    public KafkaConsumerConfig(AppointmentKafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, this::resolveDeadLetterTopic);
    }

    TopicPartition resolveDeadLetterTopic(ConsumerRecord<?, ?> record, Exception exception) {
        String suffix = isDeserializationFailure(exception)
                ? kafkaProperties.getDlt().getDeserializationSuffix()
                : kafkaProperties.getDlt().getSuffix();
        return new TopicPartition(record.topic() + suffix, -1);
    }

    boolean isDeserializationFailure(Throwable exception) {
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
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recover(deadLetterPublishingRecoverer, cancelAppointmentUseCase),
                new FixedBackOff(kafkaProperties.getRetry().getBackoffIntervalMs(), kafkaProperties.getRetry().getMaxAttempts())
        );

        errorHandler.addNotRetryableExceptions(DeserializationException.class);

        return errorHandler;
    }

    ConsumerRecordRecoverer recover(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
            CancelAppointmentDueToProcessingFailureUseCase cancelAppointmentUseCase
    ) {
        return (record, exception) -> {
            if (record.value() instanceof AppointmentAvroEvent event) {
                cancelAppointmentUseCase.execute(event.getId());
            }

            deadLetterPublishingRecoverer.accept(record, exception);
        };
    }
}
