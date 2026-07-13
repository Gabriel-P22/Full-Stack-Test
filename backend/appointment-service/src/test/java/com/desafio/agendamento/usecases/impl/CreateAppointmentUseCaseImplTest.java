package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @InjectMocks
    private CreateAppointmentUseCaseImpl useCase;

    private Appointment newAppointment() {
        return new Appointment(
                null,
                "John Doe",
                "52998224725",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    void shouldPersistEntityMappedFromDomainAndReturnSavedAppointment() {
        Appointment request = newAppointment();
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(new Appointment(
                UUID.randomUUID(), request.patientName(), request.patientCpf(),
                request.scheduledAt(), Status.PENDING, null, request.createdAt()
        ));

        when(repository.create(any())).thenReturn(createdEntity);

        ArgumentCaptor<AppointmentEntity> entityCaptor = ArgumentCaptor.forClass(AppointmentEntity.class);
        Appointment result = useCase.execute(request);

        verify(repository).create(entityCaptor.capture());
        assertThat(entityCaptor.getValue().toDomain().patientCpf()).isEqualTo(request.patientCpf());
        assertThat(result).isEqualTo(createdEntity.toDomain());
    }

    @Test
    void shouldReturnAppointmentWithGeneratedId() {
        Appointment request = newAppointment();
        UUID generatedId = UUID.randomUUID();
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(new Appointment(
                generatedId, request.patientName(), request.patientCpf(),
                request.scheduledAt(), Status.PENDING, null, request.createdAt()
        ));

        when(repository.create(any())).thenReturn(createdEntity);

        Appointment result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(generatedId);
    }

    @Test
    void shouldKeepStatusPendingOnCreation() {
        Appointment request = newAppointment();
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(new Appointment(
                UUID.randomUUID(), request.patientName(), request.patientCpf(),
                request.scheduledAt(), Status.PENDING, null, request.createdAt()
        ));

        when(repository.create(any())).thenReturn(createdEntity);

        Appointment result = useCase.execute(request);

        assertThat(result.status()).isEqualTo(Status.PENDING);
    }
}
