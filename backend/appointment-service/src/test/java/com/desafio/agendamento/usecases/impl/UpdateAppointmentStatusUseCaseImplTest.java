package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.exceptions.AppointmentAlreadyCanceledException;
import com.desafio.agendamento.frameworks.exceptions.AppointmentCancellationObservationRequiredException;
import com.desafio.agendamento.frameworks.exceptions.AppointmentNotFoundException;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAppointmentStatusUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @InjectMocks
    private UpdateAppointmentStatusUseCaseImpl useCase;

    private AppointmentEntity entityWithStatus(UUID id, Status status) {
        return AppointmentEntity.fromDomain(new Appointment(
                id,
                "John Doe",
                "52998224725",
                LocalDateTime.now().plusDays(1),
                status,
                null,
                LocalDateTime.now()
        ));
    }

    @Test
    void shouldThrowWhenAppointmentNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, Status.CONFIRMED, null))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(repository, never()).update(any());
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
    }

    @Test
    void shouldCancelWithObservation() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        Appointment result = useCase.execute(id, Status.CANCELED, "patient requested cancellation");

        assertThat(result.status()).isEqualTo(Status.CANCELED);
        assertThat(result.observation()).isEqualTo("patient requested cancellation");
        verify(repository).update(any());
    }

    @Test
    void shouldThrowWhenCancelingWithoutObservation() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        assertThatThrownBy(() -> useCase.execute(id, Status.CANCELED, null))
                .isInstanceOf(AppointmentCancellationObservationRequiredException.class);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldThrowWhenCancelingWithBlankObservation() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        assertThatThrownBy(() -> useCase.execute(id, Status.CANCELED, "   "))
                .isInstanceOf(AppointmentCancellationObservationRequiredException.class);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldThrowWhenAppointmentAlreadyCanceled() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.CANCELED)));

        assertThatThrownBy(() -> useCase.execute(id, Status.CONFIRMED, null))
                .isInstanceOf(AppointmentAlreadyCanceledException.class);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldThrowWhenAppointmentAlreadyCanceledEvenWhenTargetIsAlsoCanceled() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.CANCELED)));

        assertThatThrownBy(() -> useCase.execute(id, Status.CANCELED, "observation"))
                .isInstanceOf(AppointmentAlreadyCanceledException.class);

        verify(repository, never()).update(any());
    }
}
