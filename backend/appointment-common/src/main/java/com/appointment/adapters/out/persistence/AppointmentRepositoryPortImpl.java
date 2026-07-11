package com.appointment.adapters.out.persistence;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.enums.ErrorsMessages;
import com.appointment.enums.Status;
import com.appointment.frameworks.exceptions.AppointmentConflictException;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentRepositoryPortImpl implements AppointmentRepositoryPort {

    private final AppointmentRepository repository;

    public AppointmentRepositoryPortImpl(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public AppointmentEntity create(AppointmentEntity entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            if (entity.getIdempotencyKey() != null) {
                return repository.findByIdempotencyKey(entity.getIdempotencyKey())
                        .orElseThrow(() -> new AppointmentConflictException(ErrorsMessages.APPOINTMENT_SLOT_UNAVAILABLE.getMessage()));
            }

            throw new AppointmentConflictException(ErrorsMessages.APPOINTMENT_SLOT_UNAVAILABLE.getMessage());
        }
    }

    @Override
    public AppointmentEntity update(AppointmentEntity entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new AppointmentConflictException(ErrorsMessages.APPOINTMENT_SLOT_UNAVAILABLE.getMessage());
        }
    }

    @Override
    public boolean existsActiveAppointmentAt(LocalDateTime scheduledAt) {
        return repository.existsByScheduledAtAndStatusNot(scheduledAt, Status.CANCELED);
    }

    @Override
    public boolean existsActiveAppointmentAtExcludingId(LocalDateTime scheduledAt, UUID id) {
        return repository.existsByScheduledAtAndStatusNotAndIdNot(scheduledAt, Status.CANCELED, id);
    }

    @Override
    public Optional<AppointmentEntity> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<AppointmentEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<AppointmentEntity> findAll(Status status, Pageable pageable) {
        return status != null ? repository.findByStatus(status, pageable) : repository.findAll(pageable);
    }
}
