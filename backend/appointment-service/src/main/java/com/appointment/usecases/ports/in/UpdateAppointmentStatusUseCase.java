package com.appointment.usecases.ports.in;

import com.appointment.entities.Appointment;
import com.appointment.enums.Status;

import java.util.UUID;

public interface UpdateAppointmentStatusUseCase {
    Appointment execute(UUID id, Status status, String observation);
}
