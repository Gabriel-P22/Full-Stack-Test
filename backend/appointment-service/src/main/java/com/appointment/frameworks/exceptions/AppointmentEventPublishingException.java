package com.appointment.frameworks.exceptions;

public class AppointmentEventPublishingException extends RuntimeException {
    public AppointmentEventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
