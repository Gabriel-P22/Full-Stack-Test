package com.appointment.adapters.in.controller.dtos;

import com.appointment.enums.Status;
import com.appointment.frameworks.validation.ValidEnum;
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
