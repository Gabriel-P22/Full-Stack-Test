package com.appointment.entities.exceptions;

public class AppointmentAlreadyCanceledException extends RuntimeException {
    public AppointmentAlreadyCanceledException(String message) {
        super(message);
    }
}
