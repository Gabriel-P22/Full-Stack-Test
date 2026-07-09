package com.appointment.enums;

public enum ErrorsMessages {
    TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED(
            "It is not possible to cancel an appointment that has already been cancelled."
    ),
    TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION(
            "Not possible to cancel a appointment without the observation field"
    );

    private final String message;

    ErrorsMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
