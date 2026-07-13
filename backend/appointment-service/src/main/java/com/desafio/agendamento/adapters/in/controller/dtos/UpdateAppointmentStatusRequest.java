package com.desafio.agendamento.adapters.in.controller.dtos;

import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.validation.ValidEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull
        @ValidEnum(enumClass = Status.class)
        String status,
        String observation
) {
        public Status toStatus() {
                return Status.valueOf(status);
        }
}
