package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentDueToProcessingFailureUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @InjectMocks
    private CancelAppointmentDueToProcessingFailureUseCaseImpl useCase;

    private AppointmentEntity entityWithStatus(UUID id, Status status) {
        return AppointmentEntity.fromDomain(new Appointment(
                id,
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                status,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                null
        ));
    }

    @Test
    void shouldDoNothingWhenAppointmentNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        useCase.execute(id);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldDoNothingWhenAppointmentAlreadyCanceled() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.CANCELED)));

        useCase.execute(id);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldCancelActiveAppointmentWithProcessingFailureReason() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        useCase.execute(id);

        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(repository).update(captor.capture());
        Appointment updated = captor.getValue().toDomain();
        assertThat(updated.status()).isEqualTo(Status.CANCELED);
        assertThat(updated.observation()).isPresent();
    }

    @Test
    void shouldSwallowExceptionsWithoutPropagating() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new RuntimeException("db down"));

        assertThatCode(() -> useCase.execute(id)).doesNotThrowAnyException();
    }
}
