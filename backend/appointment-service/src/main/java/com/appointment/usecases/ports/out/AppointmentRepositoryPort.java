package com.appointment.usecases.ports.out;


import com.appointment.adapters.out.persistence.entity.AppointmentEntity;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AppointmentRepositoryPort {
    AppointmentEntity create(AppointmentEntity appointment);
    boolean existsActiveAppointmentAt(LocalDateTime scheduledAt);
    Optional<AppointmentEntity> findByIdempotencyKey(String idempotencyKey);
}
