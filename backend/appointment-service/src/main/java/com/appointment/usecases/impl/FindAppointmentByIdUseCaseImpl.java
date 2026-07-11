package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.ErrorsMessages;
import com.appointment.frameworks.exceptions.AppointmentNotFoundException;
import com.appointment.usecases.ports.in.FindAppointmentByIdUseCase;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FindAppointmentByIdUseCaseImpl implements FindAppointmentByIdUseCase {

    private final AppointmentRepositoryPort repository;

    public FindAppointmentByIdUseCaseImpl(AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Appointment execute(UUID id) {
        return repository.findById(id)
                .map(AppointmentEntity::toDomain)
                .orElseThrow(() -> new AppointmentNotFoundException(ErrorsMessages.APPOINTMENT_NOT_FOUND.getMessage()));
    }
}
