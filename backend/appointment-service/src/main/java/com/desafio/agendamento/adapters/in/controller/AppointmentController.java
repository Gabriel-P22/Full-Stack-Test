package com.desafio.agendamento.adapters.in.controller;

import com.desafio.agendamento.adapters.in.controller.dtos.ApiResponse;
import com.desafio.agendamento.adapters.in.controller.dtos.AppointmentRequest;
import com.desafio.agendamento.adapters.in.controller.dtos.AppointmentResponse;
import com.desafio.agendamento.adapters.in.controller.dtos.UpdateAppointmentStatusRequest;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.usecases.ports.in.CreateAppointmentUseCase;
import com.desafio.agendamento.usecases.ports.in.FindAppointmentByIdUseCase;
import com.desafio.agendamento.usecases.ports.in.ListAppointmentsUseCase;
import com.desafio.agendamento.usecases.ports.in.UpdateAppointmentStatusUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final ListAppointmentsUseCase listAppointmentsUseCase;
    private final FindAppointmentByIdUseCase findAppointmentByIdUseCase;
    private final UpdateAppointmentStatusUseCase updateAppointmentStatusUseCase;

    public AppointmentController(
            CreateAppointmentUseCase createAppointmentUseCase,
            ListAppointmentsUseCase listAppointmentsUseCase,
            FindAppointmentByIdUseCase findAppointmentByIdUseCase,
            UpdateAppointmentStatusUseCase updateAppointmentStatusUseCase
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.listAppointmentsUseCase = listAppointmentsUseCase;
        this.findAppointmentByIdUseCase = findAppointmentByIdUseCase;
        this.updateAppointmentStatusUseCase = updateAppointmentStatusUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(@Valid @RequestBody AppointmentRequest request) {
        Appointment created = createAppointmentUseCase.execute(request.toEntity());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(AppointmentResponse.fromDomain(created), "Appointment created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<AppointmentResponse>>> list(
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10, sort = "scheduledAt") Pageable pageable
    ) {
        Page<AppointmentResponse> page = listAppointmentsUseCase.execute(status, AppointmentSortSanitizer.sanitize(pageable))
                .map(AppointmentResponse::fromDomain);

        return ResponseEntity.ok(ApiResponse.of(new PagedModel<>(page), "Appointments retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> findById(@PathVariable UUID id) {
        Appointment appointment = findAppointmentByIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.of(AppointmentResponse.fromDomain(appointment), "Appointment retrieved successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        Appointment updated = updateAppointmentStatusUseCase.execute(id, request.toStatus(), request.observation());

        return ResponseEntity.ok(ApiResponse.of(AppointmentResponse.fromDomain(updated), "Appointment status updated successfully"));
    }

}
