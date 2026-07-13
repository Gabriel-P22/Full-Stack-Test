package com.desafio.agendamento.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public record Appointment(
        UUID id,
        String patientName,
        String patientCpf,
        LocalDateTime scheduledAt,
        Status status,
        String observation,
        LocalDateTime createdAt
) {
}
