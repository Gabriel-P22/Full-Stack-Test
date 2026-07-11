package com.appointment.usecases.ports.out;


import com.appointment.adapters.out.persistence.entity.AppointmentEntity;

import java.time.LocalDateTime;

public interface AppointmentRepositoryPort {
    AppointmentEntity create(AppointmentEntity appointment);
    boolean existsActiveAppointmentAt(LocalDateTime scheduledAt);
}
