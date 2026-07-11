package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.in.ListAppointmentsUseCase;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListAppointmentsUseCaseImpl implements ListAppointmentsUseCase {

    private final AppointmentRepositoryPort repository;

    public ListAppointmentsUseCaseImpl(AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Page<Appointment> execute(Status status, Pageable pageable) {
        Page<AppointmentEntity> entities = repository.findAll(status, pageable);
        return entities.map(AppointmentEntity::toDomain);
    }
}
