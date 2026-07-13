package com.desafio.agendamento.adapters.out.persistence;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        return repository.save(entity);
    }

    @Override
    public AppointmentEntity update(AppointmentEntity entity) {
        return repository.save(entity);
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
