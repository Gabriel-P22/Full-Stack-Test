package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessAppointmentUseCaseImplTest {

    private final ProcessAppointmentUseCaseImpl useCase = new ProcessAppointmentUseCaseImpl();

    @Test
    void shouldConfirmAPendingAppointment() {
        Appointment pending = new Appointment(
                UUID.randomUUID(), "John Doe", "52998224725",
                LocalDateTime.now().plusDays(1), Status.PENDING, null, LocalDateTime.now()
        );

        Appointment result = useCase.execute(pending);

        assertThat(result.status()).isEqualTo(Status.CONFIRMED);
    }

    @Test
    void shouldPreserveAllOtherFieldsUnchanged() {
        UUID id = UUID.randomUUID();
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now();
        Appointment pending = new Appointment(id, "John Doe", "52998224725", scheduledAt, Status.PENDING, null, createdAt);

        Appointment result = useCase.execute(pending);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.patientName()).isEqualTo("John Doe");
        assertThat(result.patientCpf()).isEqualTo("52998224725");
        assertThat(result.scheduledAt()).isEqualTo(scheduledAt);
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldReturnConfirmedRegardlessOfIncomingStatus() {
        Appointment alreadyConfirmed = new Appointment(
                UUID.randomUUID(), "John Doe", "52998224725",
                LocalDateTime.now().plusDays(1), Status.CONFIRMED, null, LocalDateTime.now()
        );

        Appointment result = useCase.execute(alreadyConfirmed);

        assertThat(result.status()).isEqualTo(Status.CONFIRMED);
    }
}
