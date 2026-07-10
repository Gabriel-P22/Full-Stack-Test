package com.appointment.usecases.ports.out;

import com.appointment.entities.Appointment;

public interface AppointmentEventProducer {
    public void execute(final Appointment appointment);
}
