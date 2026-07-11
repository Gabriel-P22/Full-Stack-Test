package com.appointment.adapters.in.controller;

import com.appointment.adapters.out.persistence.AppointmentRepository;
import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.frameworks.spring.AppointmentApplication;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppointmentApplication.class)
@AutoConfigureMockMvc
@Transactional
class AppointmentQueryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepositoryPort appointmentRepositoryPort;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private UUID pendingId;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();

        pendingId = persist(Status.PENDING, 1).id();
        persist(Status.CONFIRMED, 2);
        persist(Status.CANCELED, 3);
    }

    private Appointment persist(Status status, long daySlotOffset) {
        AppointmentEntity entity = AppointmentEntity.fromDomain(new Appointment(
                null,
                "52998224725",
                "John Doe",
                LocalDateTime.now().plusDays(daySlotOffset).truncatedTo(ChronoUnit.SECONDS),
                status,
                Optional.empty(),
                LocalDateTime.now(),
                null,
                null
        ));

        return appointmentRepositoryPort.create(entity).toDomain();
    }

    @Test
    void shouldListAllAppointmentsWithoutFilter() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    void shouldListAppointmentsFilteredByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/appointments").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    void shouldRespectPagination() throws Exception {
        mockMvc.perform(get("/api/v1/appointments").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void shouldFindAppointmentById() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/{id}", pendingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(pendingId.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
