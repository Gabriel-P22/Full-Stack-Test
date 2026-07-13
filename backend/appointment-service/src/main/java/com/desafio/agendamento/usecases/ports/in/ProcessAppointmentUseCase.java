package com.desafio.agendamento.usecases.ports.in;

import com.desafio.agendamento.entities.Appointment;

public interface ProcessAppointmentUseCase {
    Appointment execute(Appointment appointment);
}
