package com.appointment.adapters.out.persistence;

import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.frameworks.exceptions.AppointmentConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentRepositoryPortImplTest {

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentRepositoryPortImpl port;

    private AppointmentEntity anEntity(String idempotencyKey) {
        return AppointmentEntity.fromDomain(new Appointment(
                UUID.randomUUID(),
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                idempotencyKey
        ));
    }

    @Test
    void shouldReturnSavedEntityOnCreate() {
        AppointmentEntity entity = anEntity(null);
        when(repository.save(entity)).thenReturn(entity);

        assertThat(port.create(entity)).isEqualTo(entity);
    }

    @Test
    void shouldReturnExistingEntityWhenCreateConflictsButIdempotencyKeyMatches() {
        AppointmentEntity entity = anEntity("idem-key");
        when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate slot"));
        when(repository.findByIdempotencyKey("idem-key")).thenReturn(Optional.of(entity));

        assertThat(port.create(entity)).isEqualTo(entity);
    }

    @Test
    void shouldThrowConflictWhenCreateConflictsAndIdempotencyKeyIsAbsent() {
        AppointmentEntity entity = anEntity(null);
        when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate slot"));

        assertThatThrownBy(() -> port.create(entity))
                .isInstanceOf(AppointmentConflictException.class);
    }

    @Test
    void shouldThrowConflictWhenCreateConflictsAndIdempotencyKeyIsNotFound() {
        AppointmentEntity entity = anEntity("idem-key");
        when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate slot"));
        when(repository.findByIdempotencyKey("idem-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> port.create(entity))
                .isInstanceOf(AppointmentConflictException.class);
    }

    @Test
    void shouldReturnSavedEntityOnUpdate() {
        AppointmentEntity entity = anEntity(null);
        when(repository.save(entity)).thenReturn(entity);

        assertThat(port.update(entity)).isEqualTo(entity);
    }

    @Test
    void shouldThrowConflictWhenUpdateViolatesActiveSlotConstraint() {
        AppointmentEntity entity = anEntity(null);
        when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate slot"));

        assertThatThrownBy(() -> port.update(entity))
                .isInstanceOf(AppointmentConflictException.class);
    }
}
