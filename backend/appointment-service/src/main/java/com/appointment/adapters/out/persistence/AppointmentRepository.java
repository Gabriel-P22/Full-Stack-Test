package com.appointment.adapters.out.persistence;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    boolean existsByScheduledAtAndStatusNot(LocalDateTime scheduledAt, Status status);
    Optional<AppointmentEntity> findByIdempotencyKey(String idempotencyKey);
    Page<AppointmentEntity> findByStatus(Status status, Pageable pageable);
}
