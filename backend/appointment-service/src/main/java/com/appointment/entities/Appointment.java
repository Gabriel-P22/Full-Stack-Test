package com.appointment.entities;

import com.appointment.enums.Status;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.appointment.enums.ErrorsMessages.TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED;
import static com.appointment.enums.ErrorsMessages.TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION;


public record Appointment(
        UUID id,
        @NotBlank
        @CPF(message = "${response.error-messages.invalid-cpf}")
        String patientCpf,
        String patientName,
        @NotNull
        @Future(message = "${response.error-messages.appointment-with-invalid-date}")
        LocalDateTime scheduledAt,
        Status status,
        Optional<String> observation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String idempotencyKey
) {

    public Appointment updateStatus(Status status) throws Exception {
        if (this.status.equals(Status.CANCELED)) {
            throw new Exception(TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED.getMessage());
        }

        return new Appointment(
                id,
                patientCpf,
                patientName,
                scheduledAt,
                status,
                observation,
                createdAt,
                LocalDateTime.now(),
                idempotencyKey
        );
    }

    public Appointment cancel(String observation) throws Exception {
        if (this.status.equals(Status.CANCELED)) {
            throw new Exception(TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED.getMessage());
        }

        if (observation == null || observation.isBlank()) {
            throw new Exception(TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION.getMessage());
        }

        return new Appointment(
                id,
                patientCpf,
                patientName,
                scheduledAt,
                Status.CANCELED,
                Optional.ofNullable(observation),
                createdAt,
                updatedAt,
                idempotencyKey
        );
    }
}
