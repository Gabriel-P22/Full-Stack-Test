package com.appointment.entities;

import com.appointment.entities.exceptions.AppointmentAlreadyCanceledException;
import com.appointment.entities.exceptions.AppointmentCancellationObservationRequiredException;
import com.appointment.enums.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentTest {

    private Appointment anAppointment(Status status) {
        return new Appointment(
                UUID.randomUUID(),
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                status,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    @Test
    void shouldUpdateStatusAndRefreshUpdatedAt() {
        Appointment appointment = anAppointment(Status.PENDING);

        Appointment confirmed = appointment.updateStatus(Status.CONFIRMED);

        assertThat(confirmed.status()).isEqualTo(Status.CONFIRMED);
        assertThat(confirmed.updatedAt()).isNotNull();
        assertThat(confirmed.id()).isEqualTo(appointment.id());
    }

    @Test
    void shouldNotAllowUpdatingStatusWhenAlreadyCanceled() {
        Appointment appointment = anAppointment(Status.CANCELED);

        assertThatThrownBy(() -> appointment.updateStatus(Status.CONFIRMED))
                .isInstanceOf(AppointmentAlreadyCanceledException.class);
    }

    @Test
    void shouldCancelWithObservation() {
        Appointment appointment = anAppointment(Status.PENDING);

        Appointment canceled = appointment.cancel("patient requested cancellation");

        assertThat(canceled.status()).isEqualTo(Status.CANCELED);
        assertThat(canceled.observation()).contains("patient requested cancellation");
    }

    @Test
    void shouldNotAllowCancelingWhenAlreadyCanceled() {
        Appointment appointment = anAppointment(Status.CANCELED);

        assertThatThrownBy(() -> appointment.cancel("some reason"))
                .isInstanceOf(AppointmentAlreadyCanceledException.class);
    }

    @Test
    void shouldRequireObservationToCancel() {
        Appointment appointment = anAppointment(Status.PENDING);

        assertThatThrownBy(() -> appointment.cancel(null))
                .isInstanceOf(AppointmentCancellationObservationRequiredException.class);

        assertThatThrownBy(() -> appointment.cancel("   "))
                .isInstanceOf(AppointmentCancellationObservationRequiredException.class);
    }
}
