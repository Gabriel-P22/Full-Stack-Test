package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.exceptions.AppointmentConflictException;
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
class CreateAppointmentUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @Mock
    private AppointmentEventProducer producer;

    @InjectMocks
    private CreateAppointmentUseCaseImpl useCase;

    private Appointment newAppointment(String idempotencyKey) {
        return new Appointment(
                null,
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                idempotencyKey
        );
    }

    @Test
    void shouldReturnExistingAppointmentWhenIdempotencyKeyAlreadyExists() {
        Appointment request = newAppointment("idem-key-1");
        AppointmentEntity existingEntity = AppointmentEntity.fromDomain(
                new Appointment(UUID.randomUUID(), request.patientCpf(), request.patientName(),
                        request.scheduledAt(), Status.PENDING, Optional.empty(),
                        LocalDateTime.now(), null, "idem-key-1")
        );

        when(repository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.of(existingEntity));

        Appointment result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(existingEntity.toDomain().id());
        verify(repository, never()).existsActiveAppointmentAt(any());
        verify(repository, never()).create(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldProceedWithCreationWhenIdempotencyKeyIsNotFound() {
        Appointment request = newAppointment("idem-key-2");
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(
                new Appointment(UUID.randomUUID(), request.patientCpf(), request.patientName(),
                        request.scheduledAt(), Status.PENDING, Optional.empty(),
                        LocalDateTime.now(), null, "idem-key-2")
        );

        when(repository.findByIdempotencyKey("idem-key-2")).thenReturn(Optional.empty());
        when(repository.existsActiveAppointmentAt(request.scheduledAt())).thenReturn(false);
        when(repository.create(any())).thenReturn(createdEntity);

        Appointment result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(createdEntity.toDomain().id());
        verify(repository).create(any());
        verify(producer).execute(result);
    }

    @Test
    void shouldProceedWithCreationWhenIdempotencyKeyIsAbsent() {
        Appointment request = newAppointment(null);
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(
                new Appointment(UUID.randomUUID(), request.patientCpf(), request.patientName(),
                        request.scheduledAt(), Status.PENDING, Optional.empty(),
                        LocalDateTime.now(), null, null)
        );

        when(repository.existsActiveAppointmentAt(request.scheduledAt())).thenReturn(false);
        when(repository.create(any())).thenReturn(createdEntity);

        Appointment result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(createdEntity.toDomain().id());
        verify(repository, never()).findByIdempotencyKey(any());
        verify(producer).execute(result);
    }

    @Test
    void shouldThrowConflictWhenSlotIsAlreadyTaken() {
        Appointment request = newAppointment("idem-key-3");

        when(repository.findByIdempotencyKey("idem-key-3")).thenReturn(Optional.empty());
        when(repository.existsActiveAppointmentAt(request.scheduledAt())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(AppointmentConflictException.class);

        verify(repository, never()).create(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldPersistEntityMappedFromDomainAndPublishEvent() {
        Appointment request = newAppointment(null);
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(
                new Appointment(UUID.randomUUID(), request.patientCpf(), request.patientName(),
                        request.scheduledAt(), Status.PENDING, Optional.empty(),
                        LocalDateTime.now(), null, null)
        );

        when(repository.existsActiveAppointmentAt(request.scheduledAt())).thenReturn(false);
        when(repository.create(any())).thenReturn(createdEntity);

        ArgumentCaptor<AppointmentEntity> entityCaptor = ArgumentCaptor.forClass(AppointmentEntity.class);
        Appointment result = useCase.execute(request);

        verify(repository).create(entityCaptor.capture());
        assertThat(entityCaptor.getValue().toDomain().patientCpf()).isEqualTo(request.patientCpf());

        ArgumentCaptor<Appointment> producedCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(producer).execute(producedCaptor.capture());
        assertThat(producedCaptor.getValue().id()).isEqualTo(createdEntity.toDomain().id());
        assertThat(result).isEqualTo(createdEntity.toDomain());
    }
}
