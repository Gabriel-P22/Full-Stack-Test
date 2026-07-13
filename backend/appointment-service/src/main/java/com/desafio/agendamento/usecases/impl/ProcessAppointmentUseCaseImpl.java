package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.usecases.ports.in.ProcessAppointmentUseCase;
import org.springframework.stereotype.Component;

@Component
public class ProcessAppointmentUseCaseImpl implements ProcessAppointmentUseCase {

    @Override
    public Appointment execute(final Appointment appointment) {
        // Reaching this use case means the appointment already passed all field validations
        // (name, CPF format, future date), so it is confirmed immediately.
        return new Appointment(
                appointment.id(),
                appointment.patientName(),
                appointment.patientCpf(),
                appointment.scheduledAt(),
                Status.CONFIRMED,
                appointment.observation(),
                appointment.createdAt()
        );
    }
}
