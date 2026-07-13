package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAppointmentsUseCaseImplTest {

    @Mock
    private AppointmentRepositoryPort repository;

    @InjectMocks
    private ListAppointmentsUseCaseImpl useCase;

    private AppointmentEntity anEntity(Status status) {
        return AppointmentEntity.fromDomain(new Appointment(
                UUID.randomUUID(),
                "John Doe",
                "52998224725",
                LocalDateTime.now().plusDays(1),
                status,
                null,
                LocalDateTime.now()
        ));
    }

    @Test
    void shouldListAllAppointmentsWhenStatusIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentEntity> entityPage = new PageImpl<>(List.of(anEntity(Status.PENDING), anEntity(Status.CONFIRMED)));

        when(repository.findAll(isNull(), eq(pageable))).thenReturn(entityPage);

        Page<Appointment> result = useCase.execute(null, pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll(isNull(), eq(pageable));
    }

    @Test
    void shouldListAppointmentsFilteredByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentEntity> entityPage = new PageImpl<>(List.of(anEntity(Status.CONFIRMED)));

        when(repository.findAll(eq(Status.CONFIRMED), eq(pageable))).thenReturn(entityPage);

        Page<Appointment> result = useCase.execute(Status.CONFIRMED, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo(Status.CONFIRMED);
        verify(repository).findAll(eq(Status.CONFIRMED), any());
    }

    @Test
    void shouldReturnEmptyPageWhenNoAppointmentsMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(), eq(pageable))).thenReturn(Page.empty());

        Page<Appointment> result = useCase.execute(Status.CANCELED, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
