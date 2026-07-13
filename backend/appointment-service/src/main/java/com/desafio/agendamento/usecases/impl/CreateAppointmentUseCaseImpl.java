package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.frameworks.exceptions.AppointmentSlotUnavailableException;
import com.desafio.agendamento.frameworks.exceptions.ErrorsMessages;
import com.desafio.agendamento.usecases.ports.in.CreateAppointmentUseCase;
import com.desafio.agendamento.usecases.ports.in.ProcessAppointmentUseCase;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class CreateAppointmentUseCaseImpl implements CreateAppointmentUseCase {

    private final AppointmentRepositoryPort repository;
    private final ProcessAppointmentUseCase processAppointmentUseCase;

    public CreateAppointmentUseCaseImpl(
            final AppointmentRepositoryPort repository,
            final ProcessAppointmentUseCase processAppointmentUseCase
    ) {
        this.repository = repository;
        this.processAppointmentUseCase = processAppointmentUseCase;
    }

    @Override
    public Appointment execute(final Appointment appointment) {
        if (repository.existsActiveAppointmentAt(appointment.scheduledAt())) {
            throw new AppointmentSlotUnavailableException(ErrorsMessages.APPOINTMENT_SLOT_UNAVAILABLE.getMessage());
        }

        final Appointment processed = processAppointmentUseCase.execute(appointment);
        final AppointmentEntity saved = repository.create(AppointmentEntity.fromDomain(processed));
        return saved.toDomain();
    }

}
