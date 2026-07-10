package com.appointment.usecases.impl;


import com.appointment.adapters.out.messaging.AppointmentEventProducerImpl;
import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.usecases.ports.in.CreateAppointmentUseCase;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
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
    public Appointment execute(@Valid final Appointment appointment) throws Exception {

        final AppointmentEntity entity = AppointmentEntity.fromDomain(
                appointment
        );

        final AppointmentEntity dbEntity = repository.create(entity);
        final Appointment domain = dbEntity.toDomain();
        producer.execute(domain);

        return domain;
    }

}
