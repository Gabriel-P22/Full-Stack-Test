package com.desafio.agendamento.frameworks.exceptions;

public class AppointmentAlreadyCanceledException extends RuntimeException {
    public AppointmentAlreadyCanceledException(String message) {
        super(message);
    }
}
