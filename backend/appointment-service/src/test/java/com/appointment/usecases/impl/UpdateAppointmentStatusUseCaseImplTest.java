package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.frameworks.exceptions.AppointmentConflictException;
import com.appointment.frameworks.exceptions.AppointmentNotFoundException;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAppointmentStatusUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @Mock
    private AppointmentEventProducer producer;

    @InjectMocks
    private UpdateAppointmentStatusUseCaseImpl useCase;

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
    void shouldThrowWhenAppointmentNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, Status.CONFIRMED, null))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(repository, never()).update(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldConfirmPendingAppointment() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        Appointment result = useCase.execute(id, Status.CONFIRMED, null);

        assertThat(result.status()).isEqualTo(Status.CONFIRMED);
        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(repository).update(captor.capture());
        assertThat(captor.getValue().toDomain().status()).isEqualTo(Status.CONFIRMED);
        verifyNoInteractions(producer);
    }

    @Test
    void shouldCancelWithObservation() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        Appointment result = useCase.execute(id, Status.CANCELED, "patient requested cancellation");

        assertThat(result.status()).isEqualTo(Status.CANCELED);
        assertThat(result.observation()).contains("patient requested cancellation");
        verify(repository).update(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowConflictWhenCancelingWithoutObservation() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        assertThatThrownBy(() -> useCase.execute(id, Status.CANCELED, null))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessage("Not possible to cancel a appointment without the observation field");

        verify(repository, never()).update(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowConflictWhenAppointmentAlreadyCanceled() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.CANCELED)));

        assertThatThrownBy(() -> useCase.execute(id, Status.CONFIRMED, null))
                .isInstanceOf(AppointmentConflictException.class);

        verify(repository, never()).update(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldRepublishToQueueWhenTransitioningBackToPending() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.CONFIRMED)));

        Appointment result = useCase.execute(id, Status.PENDING, null);

        assertThat(result.status()).isEqualTo(Status.PENDING);
        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(repository).update(captor.capture());
        assertThat(captor.getValue().toDomain().status()).isEqualTo(Status.PENDING);

        ArgumentCaptor<Appointment> producedCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(producer).execute(producedCaptor.capture());
        assertThat(producedCaptor.getValue().status()).isEqualTo(Status.PENDING);
    }
}
