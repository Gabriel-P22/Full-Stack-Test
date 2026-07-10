package com.appointment.usecases.ports.out;


import com.appointment.adapters.out.persistence.entity.AppointmentEntity;

public interface AppointmentRepositoryPort {
    AppointmentEntity create(AppointmentEntity appointment);
}
