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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    void shouldMapEntityBackToDomain() {
        UUID id = UUID.randomUUID();
        Appointment original = new Appointment(
                id, "52998224725", "John Doe", LocalDateTime.now().plusDays(1),
                Status.PENDING, Optional.empty(), LocalDateTime.now(), null, "idem-key"
        );

        Appointment roundTripped = AppointmentEntity.fromDomain(original).toDomain();

        assertThat(roundTripped.id()).isEqualTo(id);
        assertThat(roundTripped.patientCpf()).isEqualTo("52998224725");
        assertThat(roundTripped.status()).isEqualTo(Status.PENDING);
        assertThat(roundTripped.idempotencyKey()).isEqualTo("idem-key");
    }

    @Test
    void shouldDelegateExistsActiveAppointmentAt() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);
        when(repository.existsByScheduledAtAndStatusNot(scheduledAt, Status.CANCELED)).thenReturn(true);

        assertThat(port.existsActiveAppointmentAt(scheduledAt)).isTrue();
    }

    @Test
    void shouldDelegateExistsActiveAppointmentAtExcludingId() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);
        UUID id = UUID.randomUUID();
        when(repository.existsByScheduledAtAndStatusNotAndIdNot(scheduledAt, Status.CANCELED, id)).thenReturn(true);

        assertThat(port.existsActiveAppointmentAtExcludingId(scheduledAt, id)).isTrue();
    }

    @Test
    void shouldDelegateFindByIdempotencyKey() {
        AppointmentEntity entity = anEntity("idem-key");
        when(repository.findByIdempotencyKey("idem-key")).thenReturn(Optional.of(entity));

        assertThat(port.findByIdempotencyKey("idem-key")).contains(entity);
    }

    @Test
    void shouldDelegateFindById() {
        AppointmentEntity entity = anEntity(null);
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        assertThat(port.findById(id)).contains(entity);
    }

    @Test
    void shouldDelegateFindAllWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentEntity> page = new PageImpl<>(List.of(anEntity(null)));
        when(repository.findAll(pageable)).thenReturn(page);

        assertThat(port.findAll(null, pageable)).isEqualTo(page);
    }

    @Test
    void shouldDelegateFindAllWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentEntity> page = new PageImpl<>(List.of(anEntity(null)));
        when(repository.findByStatus(Status.PENDING, pageable)).thenReturn(page);

        assertThat(port.findAll(Status.PENDING, pageable)).isEqualTo(page);
    }
}
