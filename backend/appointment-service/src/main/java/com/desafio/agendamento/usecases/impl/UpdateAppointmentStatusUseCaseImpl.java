package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.exceptions.AppointmentAlreadyCanceledException;
import com.desafio.agendamento.frameworks.exceptions.AppointmentCancellationObservationRequiredException;
import com.desafio.agendamento.frameworks.exceptions.AppointmentNotFoundException;
import com.desafio.agendamento.frameworks.exceptions.ErrorsMessages;
import com.desafio.agendamento.usecases.ports.in.UpdateAppointmentStatusUseCase;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateAppointmentStatusUseCaseImpl implements UpdateAppointmentStatusUseCase {

    private final AppointmentRepositoryPort repository;

    public UpdateAppointmentStatusUseCaseImpl(AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Appointment execute(UUID id, Status newStatus, String observation) {
        Appointment appointment = repository.findById(id)
                .map(AppointmentEntity::toDomain)
                .orElseThrow(() -> new AppointmentNotFoundException(ErrorsMessages.APPOINTMENT_NOT_FOUND.getMessage()));

        if (appointment.status() == Status.CANCELED) {
            throw new AppointmentAlreadyCanceledException(ErrorsMessages.TRY_CANCEL_APPOINTMENT_ALREADY_CANCELED.getMessage());
        }

        if (newStatus == Status.CANCELED && (observation == null || observation.isBlank())) {
            throw new AppointmentCancellationObservationRequiredException(ErrorsMessages.TRY_CANCEL_APPOINTMENT_WITHOUT_OBSERVATION.getMessage());
        }

        Appointment updated = new Appointment(
                appointment.id(),
                appointment.patientName(),
                appointment.patientCpf(),
                appointment.scheduledAt(),
                newStatus,
                newStatus == Status.CANCELED ? observation : appointment.observation(),
                appointment.createdAt()
        );

        repository.update(AppointmentEntity.fromDomain(updated));
        return updated;
    }
}
