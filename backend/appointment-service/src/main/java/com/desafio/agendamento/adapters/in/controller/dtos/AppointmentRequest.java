package com.desafio.agendamento.adapters.in.controller.dtos;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotBlank
        @Size(min = 3, message = "must be at least 3 characters long")
        @Schema(example = "John Doe")
        String patientName,

        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "${response.error-messages.invalid-cpf}")
        @Schema(example = "52998224725")
        String patientCpf,

        @NotNull
        @Future(message = "${response.error-messages.appointment-with-invalid-date}")
        @Schema(example = "2030-01-01T10:00:00")
        LocalDateTime scheduledAt
) {
        public Appointment toEntity() {
                return new Appointment(
                        null,
                        patientName,
                        patientCpf,
                        scheduledAt,
                        Status.PENDING,
                        null,
                        LocalDateTime.now()
                );
        }
}
