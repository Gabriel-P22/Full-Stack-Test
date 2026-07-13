package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.exceptions.AppointmentNotFoundException;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAppointmentByIdUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @InjectMocks
    private FindAppointmentByIdUseCaseImpl useCase;

    @Test
    void shouldReturnAppointmentWhenFound() {
        UUID id = UUID.randomUUID();
        AppointmentEntity entity = AppointmentEntity.fromDomain(new Appointment(
                id, "John Doe", "52998224725", LocalDateTime.now().plusDays(1),
                Status.PENDING, null, LocalDateTime.now()
        ));

        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Appointment result = useCase.execute(id);

        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void shouldThrowNotFoundWhenAppointmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
    }
}
