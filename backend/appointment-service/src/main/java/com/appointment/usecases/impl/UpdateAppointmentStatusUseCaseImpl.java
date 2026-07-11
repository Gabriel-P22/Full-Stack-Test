package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.entities.exceptions.AppointmentAlreadyCanceledException;
import com.appointment.entities.exceptions.AppointmentCancellationObservationRequiredException;
import com.appointment.enums.ErrorsMessages;
import com.appointment.enums.Status;
import com.appointment.frameworks.exceptions.AppointmentConflictException;
import com.appointment.frameworks.exceptions.AppointmentNotFoundException;
import com.appointment.usecases.ports.in.UpdateAppointmentStatusUseCase;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateAppointmentStatusUseCaseImpl implements UpdateAppointmentStatusUseCase {

    private final AppointmentRepositoryPort repository;
    private final AppointmentEventProducer producer;

    public UpdateAppointmentStatusUseCaseImpl(AppointmentRepositoryPort repository, AppointmentEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Override
    public Appointment execute(UUID id, Status status, String observation) {
        Appointment appointment = repository.findById(id)
                .map(AppointmentEntity::toDomain)
                .orElseThrow(() -> new AppointmentNotFoundException(ErrorsMessages.APPOINTMENT_NOT_FOUND.getMessage()));

        Appointment updated = transitionTo(appointment, status, observation);
        repository.update(AppointmentEntity.fromDomain(updated));

        if (status == Status.PENDING) {
            producer.execute(updated);
        }

        return updated;
    }

    private Appointment transitionTo(Appointment appointment, Status status, String observation) {
        try {
            return status == Status.CANCELED
                    ? appointment.cancel(observation)
                    : appointment.updateStatus(status);
        } catch (AppointmentAlreadyCanceledException | AppointmentCancellationObservationRequiredException ex) {
            throw new AppointmentConflictException(ex.getMessage());
        }
    }
}
