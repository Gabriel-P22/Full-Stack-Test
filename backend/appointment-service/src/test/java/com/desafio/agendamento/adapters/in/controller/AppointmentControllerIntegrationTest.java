package com.desafio.agendamento.adapters.in.controller;

import com.desafio.agendamento.adapters.in.controller.dtos.AppointmentRequest;
import com.desafio.agendamento.adapters.in.controller.dtos.UpdateAppointmentStatusRequest;
import com.desafio.agendamento.adapters.out.persistence.entity.AppointmentEntity;
import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import com.desafio.agendamento.frameworks.spring.AppointmentApplication;
import com.desafio.agendamento.usecases.ports.out.AppointmentRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppointmentApplication.class)
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerIntegrationTest {

    private static final String VALID_CPF = "52998224725";
    private static final String INVALID_CPF = "123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepositoryPort appointmentRepositoryPort;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private AppointmentRequest validRequest(LocalDateTime scheduledAt) {
        return new AppointmentRequest("John Doe", VALID_CPF, scheduledAt);
    }

    private LocalDateTime futureSlot(long offsetMinutes) {
        return LocalDateTime.now().plusDays(1).plusMinutes(offsetMinutes).truncatedTo(ChronoUnit.SECONDS);
    }

    private Appointment persist(Status status, long daySlotOffset) {
        AppointmentEntity entity = AppointmentEntity.fromDomain(new Appointment(
                null,
                "John Doe",
                VALID_CPF,
                LocalDateTime.now().plusDays(daySlotOffset).truncatedTo(ChronoUnit.SECONDS),
                status,
                null,
                LocalDateTime.now()
        ));

        return appointmentRepositoryPort.create(entity).toDomain();
    }

    @Test
    void shouldCreateAppointmentSuccessfully() throws Exception {
        AppointmentRequest request = validRequest(futureSlot(0));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.patientName").value("John Doe"))
                .andExpect(jsonPath("$.data.patientCpf").value(VALID_CPF))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldReturnBadRequestForInvalidCpf() throws Exception {
        AppointmentRequest request = new AppointmentRequest("John Doe", INVALID_CPF, futureSlot(30));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForPastDate() throws Exception {
        AppointmentRequest request = validRequest(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNameShorterThanThreeCharacters() throws Exception {
        AppointmentRequest request = new AppointmentRequest("Jo", VALID_CPF, futureSlot(0));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListAllAppointmentsWithoutFilter() throws Exception {
        persist(Status.PENDING, 1);
        persist(Status.CONFIRMED, 2);
        persist(Status.CANCELED, 3);

        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.page.totalElements").value(3));
    }

    @Test
    void shouldListAppointmentsFilteredByStatus() throws Exception {
        persist(Status.PENDING, 1);
        persist(Status.CONFIRMED, 2);

        mockMvc.perform(get("/api/v1/appointments").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    void shouldRespectPagination() throws Exception {
        persist(Status.PENDING, 1);
        persist(Status.CONFIRMED, 2);
        persist(Status.CANCELED, 3);

        mockMvc.perform(get("/api/v1/appointments").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(3));
    }

    @Test
    void shouldFindAppointmentById() throws Exception {
        UUID pendingId = persist(Status.PENDING, 1).id();

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

    @Test
    void shouldUpdateStatusFromPendingToConfirmed() throws Exception {
        UUID id = persist(Status.PENDING, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CONFIRMED.name(), null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void shouldUpdateStatusToCanceledWithObservation() throws Exception {
        UUID id = persist(Status.PENDING, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CANCELED.name(), "patient requested cancellation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"))
                .andExpect(jsonPath("$.data.observation").value("patient requested cancellation"));
    }

    @Test
    void shouldReturnBadRequestWhenCancelingWithoutObservation() throws Exception {
        UUID id = persist(Status.PENDING, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CANCELED.name(), null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenUpdatingAlreadyCanceledAppointment() throws Exception {
        UUID id = persist(Status.CANCELED, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CONFIRMED.name(), null))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestForStatusOutsideEnum() throws Exception {
        UUID id = persist(Status.PENDING, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest("FOOBAR", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.status").value("must be one of: PENDING, CONFIRMED, CANCELED"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfUnknownId() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/{id}/status", UUID.randomUUID())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CONFIRMED.name(), null))))
                .andExpect(status().isNotFound());
    }
}
