package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.ErrorsMessages;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.in.CancelAppointmentDueToProcessingFailureUseCase;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CancelAppointmentDueToProcessingFailureUseCaseImpl implements CancelAppointmentDueToProcessingFailureUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelAppointmentDueToProcessingFailureUseCaseImpl.class);

    private final AppointmentRepositoryPort repository;

    public CancelAppointmentDueToProcessingFailureUseCaseImpl(AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void execute(UUID appointmentId) {
        try {
            AppointmentEntity entity = repository.findById(appointmentId).orElse(null);
            if (entity == null) {
                log.warn("Appointment {} not found while canceling after exhausted retries", appointmentId);
                return;
            }

            Appointment domain = entity.toDomain();
            if (domain.status() == Status.CANCELED) {
                return;
            }

            Appointment canceled = domain.cancel(
                    ErrorsMessages.APPOINTMENT_CANCELED_DUE_TO_PROCESSING_FAILURE.getMessage()
            );

            repository.update(AppointmentEntity.fromDomain(canceled));
            log.info("Appointment {} canceled after exhausting retries", appointmentId);
        } catch (Exception ex) {
            log.error("Failed to cancel appointment {} after exhausted retries", appointmentId, ex);
        }
    }
}
