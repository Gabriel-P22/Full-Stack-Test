package com.appointment.entities;

import com.appointment.enums.Status;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.appointment.enums.ErrorsMessages.TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED;
import static com.appointment.enums.ErrorsMessages.TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION;


public record Appointment(
        UUID id,
        String patientCpf,
        String patientName,
        LocalDateTime scheduledAt,
        Status status,
        Optional<String> observation,
        LocalDateTime createdAt
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
                createdAt
        );
    }

    public Appointment cancel(String observation) throws Exception {
        if (this.status.equals(Status.CANCELED)) {
            throw new Exception(TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED.getMessage());
        }

        if (observation.isBlank()) {
            throw new Exception(TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION.getMessage());
        }

        return new Appointment(
                id,
                patientCpf,
                patientName,
                scheduledAt,
                Status.CANCELED,
                Optional.of(observation),
                createdAt
        );
    }
}
