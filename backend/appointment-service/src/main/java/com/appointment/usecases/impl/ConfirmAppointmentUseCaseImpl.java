package com.appointment.usecases.impl;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.in.ConfirmAppointmentUseCase;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ConfirmAppointmentUseCaseImpl implements ConfirmAppointmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmAppointmentUseCaseImpl.class);

    private final AppointmentRepositoryPort repository;

    public ConfirmAppointmentUseCaseImpl(AppointmentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void execute(UUID appointmentId) {
        AppointmentEntity entity = repository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Appointment not found: " + appointmentId));

        Appointment domain = entity.toDomain();

        if (domain.status() != Status.PENDING) {
            log.warn("Skipping confirmation for appointment {} already in status {}", appointmentId, domain.status());
            return;
        }

        if (!domain.scheduledAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Appointment " + appointmentId + " schedule has already passed");
        }

        if (repository.existsActiveAppointmentAtExcludingId(domain.scheduledAt(), appointmentId)) {
            throw new IllegalStateException("Appointment " + appointmentId + " slot is no longer available");
        }

        Appointment confirmed;

        try {
            confirmed = domain.updateStatus(Status.CONFIRMED);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to confirm appointment " + appointmentId, ex);
        }

        repository.update(AppointmentEntity.fromDomain(confirmed));
        log.info("Appointment {} confirmed", appointmentId);
    }
}
