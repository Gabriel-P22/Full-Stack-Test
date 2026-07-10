package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.usecases.ports.in.CreateAppointmentUseCase;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

@Validated
@Component
public class CreateAppointmentUseCaseImpl implements CreateAppointmentUseCase {

    private final AppointmentRepositoryPort repository;

    public CreateAppointmentUseCaseImpl(AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Appointment execute(@Valid Appointment appointment) throws Exception {

        AppointmentEntity entity = AppointmentEntity.fromDomain(
                appointment
        );



        AppointmentEntity dbEntity = repository.create(entity);
        //CALL KAFKA PRODUCER:

        return entity.toDomain();
    }

}
