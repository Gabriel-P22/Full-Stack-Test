package com.appointment.adapters.in.controller.dtos;

import com.appointment.entities.Appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String patientName,
        LocalDateTime scheduledAt
) {
    public static AppointmentResponse fromModel(Appointment appointment) {
        return new AppointmentResponse(
                appointment.id(),
                appointment.patientName(),
                appointment.scheduledAt()
        );
    }
}
