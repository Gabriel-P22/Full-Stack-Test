package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.usecases.ports.in.ListAppointmentsUseCase;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
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
