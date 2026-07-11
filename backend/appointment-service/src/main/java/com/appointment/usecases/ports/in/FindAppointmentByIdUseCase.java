package com.appointment.usecases.ports.in;

import com.appointment.entities.Appointment;

import java.util.UUID;

public interface FindAppointmentByIdUseCase {
    Appointment execute(UUID id);
}
