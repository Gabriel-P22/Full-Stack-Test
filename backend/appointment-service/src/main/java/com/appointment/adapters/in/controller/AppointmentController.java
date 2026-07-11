package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.ApiResponse;
import com.appointment.adapters.in.controller.dtos.AppointmentRequest;
import com.appointment.adapters.in.controller.dtos.AppointmentResponse;
import com.appointment.adapters.in.controller.dtos.UpdateAppointmentStatusRequest;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.usecases.ports.in.CreateAppointmentUseCase;
import com.appointment.usecases.ports.in.FindAppointmentByIdUseCase;
import com.appointment.usecases.ports.in.ListAppointmentsUseCase;
import com.appointment.usecases.ports.in.UpdateAppointmentStatusUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final UpdateAppointmentStatusUseCase updateAppointmentStatusUseCase;
    private final AppointmentModelAssembler appointmentModelAssembler;
    private final PagedResourcesAssembler<Appointment> pagedResourcesAssembler;

    public AppointmentController(
            CreateAppointmentUseCase createAppointmentUseCase,
            ListAppointmentsUseCase listAppointmentsUseCase,
            FindAppointmentByIdUseCase findAppointmentByIdUseCase,
            UpdateAppointmentStatusUseCase updateAppointmentStatusUseCase,
            AppointmentModelAssembler appointmentModelAssembler,
            PagedResourcesAssembler<Appointment> pagedResourcesAssembler
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.listAppointmentsUseCase = listAppointmentsUseCase;
        this.findAppointmentByIdUseCase = findAppointmentByIdUseCase;
        this.updateAppointmentStatusUseCase = updateAppointmentStatusUseCase;
        this.appointmentModelAssembler = appointmentModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PostMapping("/appointment")
    public ResponseEntity<ApiResponse<EntityModel<AppointmentResponse>>> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody AppointmentRequest request
    ) {
        Appointment appointment = createAppointmentUseCase.execute(request.toEntity(idempotencyKey));

        return ResponseEntity.ok(ApiResponse.of(appointmentModelAssembler.toModel(appointment), "Appointment created successfully"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<AppointmentResponse>>>> list(
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10, sort = "scheduledAt") Pageable pageable
    ) {
        Page<Appointment> page = listAppointmentsUseCase.execute(status, pageable);
        PagedModel<EntityModel<AppointmentResponse>> pagedModel =
                pagedResourcesAssembler.toModel(page, appointmentModelAssembler::toModel);

        return ResponseEntity.ok(ApiResponse.of(pagedModel, "Appointments retrieved successfully"));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<EntityModel<AppointmentResponse>>> findById(@PathVariable UUID id) {
        Appointment appointment = findAppointmentByIdUseCase.execute(id);

        return ResponseEntity.ok(ApiResponse.of(appointmentModelAssembler.toModel(appointment), "Appointment retrieved successfully"));
    }

    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<ApiResponse<EntityModel<AppointmentResponse>>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        Appointment updated = updateAppointmentStatusUseCase.execute(id, request.status(), request.observation());

        return ResponseEntity.ok(ApiResponse.of(appointmentModelAssembler.toModel(updated), "Appointment status updated successfully"));
    }

}
