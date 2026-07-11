package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.ApiResponse;
import com.appointment.adapters.in.controller.dtos.AppointmentResponse;
import com.appointment.adapters.in.controller.dtos.PageResponse;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.in.FindAppointmentByIdUseCase;
import com.appointment.usecases.ports.in.ListAppointmentsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentQueryController {

    private final ListAppointmentsUseCase listAppointmentsUseCase;
    private final FindAppointmentByIdUseCase findAppointmentByIdUseCase;

    public AppointmentQueryController(
            ListAppointmentsUseCase listAppointmentsUseCase,
            FindAppointmentByIdUseCase findAppointmentByIdUseCase
    ) {
        this.listAppointmentsUseCase = listAppointmentsUseCase;
        this.findAppointmentByIdUseCase = findAppointmentByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> list(
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10, sort = "scheduledAt") Pageable pageable
    ) {
        Page<AppointmentResponse> page = listAppointmentsUseCase.execute(status, pageable)
                .map(AppointmentResponse::fromModel);

        return ResponseEntity.ok(ApiResponse.of(PageResponse.of(page), "Appointments retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> findById(@PathVariable UUID id) {
        Appointment appointment = findAppointmentByIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.of(AppointmentResponse.fromModel(appointment), "Appointment retrieved successfully"));
    }
}
