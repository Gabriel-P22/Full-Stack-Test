package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.ApiResponse;
import com.appointment.adapters.in.controller.dtos.AppointmentRequest;
import com.appointment.adapters.in.controller.dtos.AppointmentResponse;
import com.appointment.adapters.in.controller.dtos.PageResponse;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.in.CreateAppointmentUseCase;
import com.appointment.usecases.ports.in.FindAppointmentByIdUseCase;
import com.appointment.usecases.ports.in.ListAppointmentsUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController()
@RequestMapping("/api/v1")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final ListAppointmentsUseCase listAppointmentsUseCase;
    private final FindAppointmentByIdUseCase findAppointmentByIdUseCase;

    public AppointmentController(
            CreateAppointmentUseCase createAppointmentUseCase,
            ListAppointmentsUseCase listAppointmentsUseCase,
            FindAppointmentByIdUseCase findAppointmentByIdUseCase
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.listAppointmentsUseCase = listAppointmentsUseCase;
        this.findAppointmentByIdUseCase = findAppointmentByIdUseCase;
    }

    @PostMapping("/appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AppointmentRequest request
    ) {
        AppointmentResponse response = AppointmentResponse.fromModel(
                createAppointmentUseCase.execute(request.toEntity(idempotencyKey))
        );

        return ResponseEntity.ok(ApiResponse.of(response, "Appointment created successfully"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> list(
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10, sort = "scheduledAt") Pageable pageable
    ) {
        Page<AppointmentResponse> page = listAppointmentsUseCase.execute(status, pageable)
                .map(AppointmentResponse::fromModel);

        return ResponseEntity.ok(ApiResponse.of(PageResponse.of(page), "Appointments retrieved successfully"));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> findById(@PathVariable UUID id) {
        Appointment appointment = findAppointmentByIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.of(AppointmentResponse.fromModel(appointment), "Appointment retrieved successfully"));
    }

}
