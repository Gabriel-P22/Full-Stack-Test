package com.appointment.frameworks.config;

import com.appointment.adapters.out.messaging.dto.AppointmentAvroEvent;
import com.appointment.usecases.ports.in.CancelAppointmentDueToProcessingFailureUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig config = new KafkaConsumerConfig(new AppointmentKafkaProperties());

    @Mock
    private DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

    @Mock
    private CancelAppointmentDueToProcessingFailureUseCase cancelAppointmentUseCase;

    @Mock
    private KafkaTemplate<String, AppointmentAvroEvent> kafkaTemplate;

    @Test
    void shouldCreateDeadLetterPublishingRecovererBean() {
        assertThat(config.deadLetterPublishingRecoverer(kafkaTemplate)).isNotNull();
    }

    @Test
    void shouldCreateKafkaErrorHandlerBean() {
        CommonErrorHandler errorHandler = config.kafkaErrorHandler(deadLetterPublishingRecoverer, cancelAppointmentUseCase);

        assertThat(errorHandler).isNotNull();
    }

    @Test
    void shouldResolveRegularDeadLetterTopicForNonDeserializationFailures() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("appointment-events-topic", 0, 0L, "key", "value");

        TopicPartition topicPartition = config.resolveDeadLetterTopic(record, new RuntimeException("boom"));

        assertThat(topicPartition.topic()).isEqualTo("appointment-events-topic-dlt");
    }

    @Test
    void shouldResolveDeserializationDeadLetterTopicForDeserializationFailures() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("appointment-events-topic", 0, 0L, "key", "value");
        DeserializationException deserializationException =
                new DeserializationException("bad payload", new byte[0], false, new RuntimeException());

        TopicPartition topicPartition = config.resolveDeadLetterTopic(record, deserializationException);

        assertThat(topicPartition.topic()).isEqualTo("appointment-events-topic-deserialization-dlt");
    }

    @Test
    void shouldDetectDeserializationFailureEvenWhenWrapped() {
        DeserializationException deserializationException =
                new DeserializationException("bad payload", new byte[0], false, new RuntimeException());
        RuntimeException wrapper = new RuntimeException("wrapped", deserializationException);

        assertThat(config.isDeserializationFailure(wrapper)).isTrue();
        assertThat(config.isDeserializationFailure(new RuntimeException("unrelated"))).isFalse();
    }

    @Test
    void shouldCancelAppointmentAndDelegateToDeadLetterRecovererWhenRecordIsAnAppointmentEvent() {
        UUID id = UUID.randomUUID();
        AppointmentAvroEvent event = AppointmentAvroEvent.newBuilder().setId(id).build();
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("appointment-events-topic", 0, 0L, "key", event);
        RuntimeException exception = new RuntimeException("processing failed");

        ConsumerRecordRecoverer recoverer = config.recover(deadLetterPublishingRecoverer, cancelAppointmentUseCase);
        recoverer.accept(record, exception);

        verify(cancelAppointmentUseCase).execute(id);
        verify(deadLetterPublishingRecoverer).accept(record, exception);
    }

    @Test
    void shouldSkipCancellationWhenRecordValueIsNotAnAppointmentEvent() {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("appointment-events-topic", 0, 0L, "key", "not-an-event");
        RuntimeException exception = new RuntimeException("processing failed");

        ConsumerRecordRecoverer recoverer = config.recover(deadLetterPublishingRecoverer, cancelAppointmentUseCase);
        recoverer.accept(record, exception);

        verify(cancelAppointmentUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
        verify(deadLetterPublishingRecoverer).accept(record, exception);
    }
}
