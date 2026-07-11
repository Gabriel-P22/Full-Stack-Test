package com.appointment.usecases.ports.in;

import java.util.UUID;

public interface ConfirmAppointmentUseCase {
    void execute(UUID appointmentId);
}
