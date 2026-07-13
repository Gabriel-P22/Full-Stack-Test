package com.desafio.agendamento.adapters.in.controller.dtos;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String patientName,
        String patientCpf,
        LocalDateTime scheduledAt,
        Status status,
        String observation,
        LocalDateTime createdAt
) {
    public static AppointmentResponse fromDomain(Appointment appointment) {
        return new AppointmentResponse(
                appointment.id(),
                appointment.patientName(),
                appointment.patientCpf(),
                appointment.scheduledAt(),
                appointment.status(),
                appointment.observation(),
                appointment.createdAt()
        );
    }
}
