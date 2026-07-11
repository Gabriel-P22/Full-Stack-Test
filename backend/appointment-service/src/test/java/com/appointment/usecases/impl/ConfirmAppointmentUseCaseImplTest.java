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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmAppointmentUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @InjectMocks
    private ConfirmAppointmentUseCaseImpl useCase;

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

        assertThatThrownBy(() -> useCase.execute(id))
                .isInstanceOf(IllegalStateException.class);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldSkipUpdateWhenAppointmentIsNotPending() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.CONFIRMED)));

        useCase.execute(id);

        verify(repository, never()).update(any());
    }

    @Test
    void shouldConfirmPendingAppointment() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, Status.PENDING)));

        useCase.execute(id);

        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(repository).update(captor.capture());
        assertThat(captor.getValue().toDomain().status()).isEqualTo(Status.CONFIRMED);
    }
}
