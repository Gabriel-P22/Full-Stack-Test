package com.appointment.entities.exceptions;

public class AppointmentCancellationObservationRequiredException extends RuntimeException {
    public AppointmentCancellationObservationRequiredException(String message) {
        super(message);
    }
}
