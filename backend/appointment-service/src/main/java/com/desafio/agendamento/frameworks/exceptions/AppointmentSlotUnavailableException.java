package com.desafio.agendamento.frameworks.exceptions;

public class AppointmentSlotUnavailableException extends RuntimeException {
    public AppointmentSlotUnavailableException(String message) {
        super(message);
    }
}
