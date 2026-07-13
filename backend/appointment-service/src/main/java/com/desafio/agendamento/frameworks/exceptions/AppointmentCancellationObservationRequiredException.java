package com.desafio.agendamento.frameworks.exceptions;

public class AppointmentCancellationObservationRequiredException extends RuntimeException {
    public AppointmentCancellationObservationRequiredException(String message) {
        super(message);
    }
}
