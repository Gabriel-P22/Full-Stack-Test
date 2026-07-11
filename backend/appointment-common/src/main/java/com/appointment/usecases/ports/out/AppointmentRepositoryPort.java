package com.appointment.usecases.ports.out;


import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort {
    AppointmentEntity create(AppointmentEntity appointment);
    AppointmentEntity update(AppointmentEntity appointment);
    boolean existsActiveAppointmentAt(LocalDateTime scheduledAt);
    boolean existsActiveAppointmentAtExcludingId(LocalDateTime scheduledAt, UUID id);
    Optional<AppointmentEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<AppointmentEntity> findById(UUID id);
    Page<AppointmentEntity> findAll(Status status, Pageable pageable);
}
