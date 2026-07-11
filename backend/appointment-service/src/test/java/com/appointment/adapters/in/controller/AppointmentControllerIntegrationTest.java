package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.AppointmentRequest;
import com.appointment.adapters.out.persistence.AppointmentRepository;
import com.appointment.frameworks.spring.AppointmentApplication;
import com.appointment.usecases.ports.out.AppointmentEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
}
