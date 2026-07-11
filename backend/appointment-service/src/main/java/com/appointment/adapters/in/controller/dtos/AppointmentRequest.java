package com.appointment.adapters.in.controller.dtos;

import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.Optional;

public record AppointmentRequest(
        @NotNull
        @CPF(message = "${response.error-messages.invalid-cpf}")
        String patientCpf,

        @NotBlank
        String patientName,

        @NotNull
        @Future(message = "${response.error-messages.appointment-with-invalid-date}")
        LocalDateTime scheduledAt
) {
        public Appointment toEntity(String idempotencyKey) {
                return new Appointment(
                        null,
                        patientCpf,
                        patientName,
                        scheduledAt,
                        Status.PENDING,
                        Optional.empty(),
                        LocalDateTime.now(),
                        null,
                        idempotencyKey
                );
        }
}
