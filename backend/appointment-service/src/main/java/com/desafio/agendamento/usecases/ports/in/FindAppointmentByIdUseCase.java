package com.desafio.agendamento.usecases.ports.in;

import com.desafio.agendamento.entities.Appointment;

import java.util.UUID;

public interface FindAppointmentByIdUseCase {
    Appointment execute(UUID id);
}
