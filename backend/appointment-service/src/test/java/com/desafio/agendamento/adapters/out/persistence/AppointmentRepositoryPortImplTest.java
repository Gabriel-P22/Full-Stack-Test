package com.desafio.agendamento.adapters.out.persistence;

import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentRepositoryPortImplTest {

    @Mock
    private AppointmentRepository repository;

    @InjectMocks
    private AppointmentRepositoryPortImpl port;

    private AppointmentEntity anEntity() {
        return AppointmentEntity.fromDomain(new Appointment(
                UUID.randomUUID(),
                "John Doe",
                "52998224725",
                LocalDateTime.now().plusDays(1),
                Status.PENDING,
                null,
                LocalDateTime.now()
        ));
    }

    @Test
    void shouldReturnSavedEntityOnCreate() {
        AppointmentEntity entity = anEntity();
        when(repository.save(entity)).thenReturn(entity);

        assertThat(port.create(entity)).isEqualTo(entity);
    }

    @Test
    void shouldReturnSavedEntityOnUpdate() {
        AppointmentEntity entity = anEntity();
        when(repository.save(entity)).thenReturn(entity);

        assertThat(port.update(entity)).isEqualTo(entity);
    }

    @Test
    void shouldFindById() {
        AppointmentEntity entity = anEntity();
        when(repository.findById(entity.toDomain().id())).thenReturn(Optional.empty());

        assertThat(port.findById(entity.toDomain().id())).isEmpty();
    }

    @Test
    void shouldFindAllWithoutStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentEntity> page = new PageImpl<>(List.of(anEntity()));
        when(repository.findAll(pageable)).thenReturn(page);

        assertThat(port.findAll(null, pageable)).isEqualTo(page);
    }

    @Test
    void shouldFindAllFilteredByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppointmentEntity> page = new PageImpl<>(List.of(anEntity()));
        when(repository.findByStatus(Status.CONFIRMED, pageable)).thenReturn(page);

        assertThat(port.findAll(Status.CONFIRMED, pageable)).isEqualTo(page);
    }
}
