package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.ErrorsMessages;
import com.appointment.usecases.exceptions.AppointmentConflictException;
import com.appointment.usecases.ports.in.CreateAppointmentUseCase;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class CreateAppointmentUseCaseImpl implements CreateAppointmentUseCase {

    private final AppointmentRepositoryPort repository;
    private final AppointmentEventProducer producer;

    public CreateAppointmentUseCaseImpl(
            final AppointmentRepositoryPort repository,
            final AppointmentEventProducer producer
    ) {
        this.repository = repository;
        this.producer = producer;
    }

    @Override
    public Appointment execute(@Valid final Appointment appointment) {

        if (repository.existsActiveAppointmentAt(appointment.scheduledAt())) {
            throw new AppointmentConflictException(ErrorsMessages.APPOINTMENT_SLOT_UNAVAILABLE.getMessage());
        }

        final AppointmentEntity entity = AppointmentEntity.fromDomain(
                appointment
        );

        final AppointmentEntity dbEntity = repository.create(entity);
        final Appointment domain = dbEntity.toDomain();
        producer.execute(domain);

        return domain;
    }

}
