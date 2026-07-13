package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.frameworks.exceptions.AppointmentNotFoundException;
import com.desafio.agendamento.frameworks.exceptions.ErrorsMessages;
import com.desafio.agendamento.usecases.ports.in.FindAppointmentByIdUseCase;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
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
