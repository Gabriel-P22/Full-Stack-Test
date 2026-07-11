package com.appointment.frameworks.config;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final long RETRY_INTERVAL_MS = 1_000L;
    private static final long RETRY_MAX_ATTEMPTS = 2L;

    /**
     * Deserialization failures publish the raw undecoded bytes (Avro infers a "bytes" schema for
     * them), while processing failures republish the typed AppointmentAvroEvent ("record" schema).
     * Both can't share one DLT topic: Schema Registry rejects the second schema registered for the
     * same subject once the first one is in place (BACKWARD compatibility), so a separate topic per
     * failure kind avoids that conflict.
     */
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

    /**
     * Auto-detected by Spring Boot's ConcurrentKafkaListenerContainerFactoryConfigurer
     * and applied to the default "kafkaListenerContainerFactory" bean.
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(RETRY_INTERVAL_MS, RETRY_MAX_ATTEMPTS)
        );

        errorHandler.addNotRetryableExceptions(DeserializationException.class);

        return errorHandler;
    }
}
