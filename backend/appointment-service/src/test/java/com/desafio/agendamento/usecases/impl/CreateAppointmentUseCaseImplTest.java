package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.exceptions.AppointmentSlotUnavailableException;
import com.desafio.agendamento.usecases.ports.in.ProcessAppointmentUseCase;
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
    private ProcessAppointmentUseCase processAppointmentUseCase;

    @InjectMocks
    private CreateAppointmentUseCaseImpl useCase;

    private Appointment newAppointment(Status status) {
        return new Appointment(
                null,
                "John Doe",
                "52998224725",
                LocalDateTime.now().plusDays(1),
                status,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    void shouldDelegateToProcessAppointmentUseCaseBeforePersisting() {
        Appointment request = newAppointment(Status.PENDING);
        Appointment processed = newAppointment(Status.CONFIRMED);
        when(processAppointmentUseCase.execute(request)).thenReturn(processed);
        when(repository.create(any())).thenReturn(AppointmentEntity.fromDomain(processed));

        useCase.execute(request);

        verify(processAppointmentUseCase).execute(request);
    }

    @Test
    void shouldPersistTheProcessedAppointmentInsteadOfTheRawRequest() {
        Appointment request = newAppointment(Status.PENDING);
        Appointment processed = newAppointment(Status.CONFIRMED);
        when(processAppointmentUseCase.execute(request)).thenReturn(processed);
        when(repository.create(any())).thenReturn(AppointmentEntity.fromDomain(processed));

        ArgumentCaptor<AppointmentEntity> entityCaptor = ArgumentCaptor.forClass(AppointmentEntity.class);
        useCase.execute(request);

        verify(repository).create(entityCaptor.capture());
        assertThat(entityCaptor.getValue().toDomain().status()).isEqualTo(Status.CONFIRMED);
        assertThat(entityCaptor.getValue().toDomain().patientCpf()).isEqualTo(request.patientCpf());
    }

    @Test
    void shouldReturnTheSavedAppointmentWithGeneratedId() {
        Appointment request = newAppointment(Status.PENDING);
        Appointment processed = newAppointment(Status.CONFIRMED);
        UUID generatedId = UUID.randomUUID();
        AppointmentEntity createdEntity = AppointmentEntity.fromDomain(new Appointment(
                generatedId, processed.patientName(), processed.patientCpf(),
                processed.scheduledAt(), processed.status(), processed.observation(), processed.createdAt()
        ));

        when(processAppointmentUseCase.execute(request)).thenReturn(processed);
        when(repository.create(any())).thenReturn(createdEntity);

        Appointment result = useCase.execute(request);

        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result).isEqualTo(createdEntity.toDomain());
    }

    @Test
    void shouldThrowSlotUnavailableWhenAnActiveAppointmentAlreadyExistsAtTheSameTime() {
        Appointment request = newAppointment(Status.PENDING);
        when(repository.existsActiveAppointmentAt(request.scheduledAt())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(AppointmentSlotUnavailableException.class);
    }

    @Test
    void shouldNotProcessOrPersistWhenSlotIsUnavailable() {
        Appointment request = newAppointment(Status.PENDING);
        when(repository.existsActiveAppointmentAt(request.scheduledAt())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(AppointmentSlotUnavailableException.class);

        verifyNoInteractions(processAppointmentUseCase);
        verify(repository, never()).create(any());
    }
}
