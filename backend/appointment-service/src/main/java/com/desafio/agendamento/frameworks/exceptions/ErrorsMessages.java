package com.desafio.agendamento.frameworks.exceptions;

public enum ErrorsMessages {
    TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED(
            "It is not possible to change the status of an appointment that has already been canceled."
    ),
    TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION(
            "Not possible to cancel an appointment without the observation field"
    ),
    APPOINTMENT_NOT_FOUND(
            "Appointment not found"
    );

    private final String message;

    ErrorsMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
