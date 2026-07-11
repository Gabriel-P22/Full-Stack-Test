package com.appointment.adapters.in.controller.dtos;

import com.appointment.entities.Appointment;
import com.appointment.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String patientName,
        LocalDateTime scheduledAt,
        Status status,
        String observation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AppointmentResponse fromModel(Appointment appointment) {
        return new AppointmentResponse(
                appointment.id(),
                appointment.patientName(),
                appointment.scheduledAt(),
                appointment.status(),
                appointment.observation().orElse(null),
                appointment.createdAt(),
                appointment.updatedAt()
        );
    }
}
