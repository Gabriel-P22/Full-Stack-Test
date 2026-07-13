package com.desafio.agendamento.usecases.ports.out;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort {
    AppointmentEntity create(AppointmentEntity appointment);
    AppointmentEntity update(AppointmentEntity appointment);
    Optional<AppointmentEntity> findById(UUID id);
    Page<AppointmentEntity> findAll(Status status, Pageable pageable);
    boolean existsActiveAppointmentAt(LocalDateTime scheduledAt);
}
