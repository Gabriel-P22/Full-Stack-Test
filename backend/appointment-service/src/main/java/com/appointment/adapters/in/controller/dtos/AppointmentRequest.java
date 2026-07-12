package com.appointment.adapters.in.controller.dtos;

import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.util.Optional;

public record AppointmentRequest(
        @NotNull
        @CPF(message = "${response.error-messages.invalid-cpf}")
        @Schema(example = "529.982.247-25")
        String patientCpf,

        @NotBlank
        @Schema(example = "John Doe")
        String patientName,

        @NotNull
        @Future(message = "${response.error-messages.appointment-with-invalid-date}")
        @Schema(example = "2030-01-01T10:00:00")
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
