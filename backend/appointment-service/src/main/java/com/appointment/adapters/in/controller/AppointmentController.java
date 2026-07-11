package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.ApiResponse;
import com.appointment.adapters.in.controller.dtos.AppointmentRequest;
import com.appointment.adapters.in.controller.dtos.AppointmentResponse;
import com.appointment.usecases.ports.in.CreateAppointmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/v1/appointment")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;

    public AppointmentController(CreateAppointmentUseCase createAppointmentUseCase) {
        this.createAppointmentUseCase = createAppointmentUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AppointmentRequest request
    ) {
        AppointmentResponse response = AppointmentResponse.fromModel(
                createAppointmentUseCase.execute(request.toEntity(idempotencyKey))
        );

        return ResponseEntity.ok(ApiResponse.of(response, "Appointment created successfully"));
    }

}
