package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.AppointmentRequest;
import com.appointment.adapters.in.controller.dtos.UpdateAppointmentStatusRequest;
import com.appointment.adapters.out.persistence.AppointmentRepository;
import com.appointment.adapters.out.persistence.entity.AppointmentEntity;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import com.appointment.frameworks.spring.AppointmentApplication;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import com.appointment.usecases.ports.out.AppointmentRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private static final String INVALID_CPF = "12345678900";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentRepositoryPort appointmentRepositoryPort;

    @MockitoBean
    private AppointmentEventProducer producer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private AppointmentRequest validRequest(LocalDateTime scheduledAt) {
        return new AppointmentRequest(VALID_CPF, "John Doe", scheduledAt);
    }

    private LocalDateTime futureSlot(long offsetMinutes) {
        return LocalDateTime.now().plusDays(1).plusMinutes(offsetMinutes).truncatedTo(ChronoUnit.SECONDS);
    }

    private Appointment persist(Status status, long daySlotOffset) {
        AppointmentEntity entity = AppointmentEntity.fromDomain(new Appointment(
                null,
                VALID_CPF,
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
    void shouldCreateAppointmentSuccessfully() throws Exception {
        when(producer.execute(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentRequest request = validRequest(futureSlot(0));

        mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-success")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.patientName").value("John Doe"));

        verify(producer).execute(any());
        assertThat(appointmentRepository.findByIdempotencyKey("key-success")).isPresent();
    }

    @Test
    void shouldReturnSameAppointmentWhenIdempotencyKeyIsReplayed() throws Exception {
        when(producer.execute(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentRequest request = validRequest(futureSlot(10));
        String body = objectMapper.writeValueAsString(request);

        String firstResponse = mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-replay")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-replay")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String firstId = objectMapper.readTree(firstResponse).at("/data/id").asText();
        String secondId = objectMapper.readTree(secondResponse).at("/data/id").asText();
        assertThat(secondId).isEqualTo(firstId);

        verify(producer).execute(any());
    }

    @Test
    void shouldReturnConflictWhenSlotIsAlreadyTaken() throws Exception {
        when(producer.execute(any())).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime slot = futureSlot(20);

        mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-first")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest(slot))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-second")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest(slot))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestForInvalidCpf() throws Exception {
        AppointmentRequest request = new AppointmentRequest(INVALID_CPF, "John Doe", futureSlot(30));

        mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-invalid-cpf")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(producer, never()).execute(any());
    }

    @Test
    void shouldReturnBadRequestForPastDate() throws Exception {
        AppointmentRequest request = validRequest(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/v1/appointment")
                        .header("Idempotency-Key", "key-past-date")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(producer, never()).execute(any());
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyHeaderIsMissing() throws Exception {
        AppointmentRequest request = validRequest(futureSlot(40));

        mockMvc.perform(post("/api/v1/appointment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(producer, never()).execute(any());
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
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.links[?(@.rel=='self')]").exists())
                .andExpect(jsonPath("$.data.links[?(@.rel=='appointments')]").exists())
                .andExpect(jsonPath("$.data.links[?(@.rel=='confirm')]").exists())
                .andExpect(jsonPath("$.data.links[?(@.rel=='cancel')]").exists());
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
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CONFIRMED, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.links[?(@.rel=='cancel')]").exists())
                .andExpect(jsonPath("$.data.links[?(@.rel=='confirm')]").doesNotExist());
    }

    @Test
    void shouldUpdateStatusToCanceledWithObservation() throws Exception {
        UUID id = persist(Status.PENDING, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CANCELED, "patient requested cancellation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"))
                .andExpect(jsonPath("$.data.observation").value("patient requested cancellation"))
                .andExpect(jsonPath("$.data.links[?(@.rel=='confirm')]").doesNotExist())
                .andExpect(jsonPath("$.data.links[?(@.rel=='cancel')]").doesNotExist());
    }

    @Test
    void shouldReturnConflictWhenCancelingWithoutObservation() throws Exception {
        UUID id = persist(Status.PENDING, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CANCELED, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflictWhenUpdatingAlreadyCanceledAppointment() throws Exception {
        UUID id = persist(Status.CANCELED, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CONFIRMED, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfUnknownId() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/{id}/status", UUID.randomUUID())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.CONFIRMED, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRepublishToQueueWhenUpdatingStatusBackToPending() throws Exception {
        UUID id = persist(Status.CONFIRMED, 1).id();

        mockMvc.perform(patch("/api/v1/appointments/{id}/status", id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateAppointmentStatusRequest(Status.PENDING, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(producer).execute(any());
    }
}
