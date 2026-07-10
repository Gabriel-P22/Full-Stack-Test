package com.appointment.usecases.ports.in;

import com.appointment.entities.Appointment;
import jakarta.validation.Valid;

public interface CreateAppointmentUseCase {
    Appointment execute(@Valid final Appointment request) throws Exception;
}
