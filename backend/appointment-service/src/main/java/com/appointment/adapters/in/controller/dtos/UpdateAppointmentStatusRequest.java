package com.appointment.adapters.in.controller.dtos;

import com.appointment.enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull Status status,
        String observation
) {
}
