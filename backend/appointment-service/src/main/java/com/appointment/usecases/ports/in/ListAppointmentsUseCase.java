package com.appointment.usecases.ports.in;

import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListAppointmentsUseCase {
    Page<Appointment> execute(Status status, Pageable pageable);
}
