package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.usecases.ports.in.CreateAppointmentUseCase;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class CreateAppointmentUseCaseImpl implements CreateAppointmentUseCase {

    private final AppointmentRepositoryPort repository;

    public CreateAppointmentUseCaseImpl(final AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Appointment execute(final Appointment appointment) {
        final AppointmentEntity saved = repository.create(AppointmentEntity.fromDomain(appointment));
        return saved.toDomain();
    }

}
