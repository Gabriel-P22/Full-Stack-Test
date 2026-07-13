package com.desafio.agendamento.usecases.ports.in;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;

import java.util.UUID;

public interface UpdateAppointmentStatusUseCase {
    Appointment execute(UUID id, Status status, String observation);
}
