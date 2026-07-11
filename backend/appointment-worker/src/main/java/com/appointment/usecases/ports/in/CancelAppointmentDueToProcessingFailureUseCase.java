package com.appointment.usecases.ports.in;

import java.util.UUID;

public interface CancelAppointmentDueToProcessingFailureUseCase {
    void execute(UUID appointmentId);
}
