package com.desafio.agendamento.usecases.ports.in;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListAppointmentsUseCase {
    Page<Appointment> execute(Status status, Pageable pageable);
}
