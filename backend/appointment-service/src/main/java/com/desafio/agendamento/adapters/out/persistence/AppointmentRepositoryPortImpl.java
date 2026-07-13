package com.desafio.agendamento.adapters.out.persistence;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.exceptions.AppointmentSlotUnavailableException;
import com.desafio.agendamento.frameworks.exceptions.ErrorsMessages;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
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
        return saveOrThrowSlotUnavailable(entity);
    }

    @Override
    public AppointmentEntity update(AppointmentEntity entity) {
        return saveOrThrowSlotUnavailable(entity);
    }

    private AppointmentEntity saveOrThrowSlotUnavailable(AppointmentEntity entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new AppointmentSlotUnavailableException(ErrorsMessages.APPOINTMENT_SLOT_UNAVAILABLE.getMessage());
        }
    }

    @Override
    public Optional<AppointmentEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<AppointmentEntity> findAll(Status status, Pageable pageable) {
        return status != null ? repository.findByStatus(status, pageable) : repository.findAll(pageable);
    }

    @Override
    public boolean existsActiveAppointmentAt(LocalDateTime scheduledAt) {
        return repository.existsByScheduledAtAndStatusNot(scheduledAt, Status.CANCELED);
    }
}
