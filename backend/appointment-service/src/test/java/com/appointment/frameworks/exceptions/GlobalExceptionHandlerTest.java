package com.appointment.frameworks.exceptions;

import com.appointment.adapters.in.controller.dtos.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnServiceUnavailableForDataAccessException() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleDataAccessException(new QueryTimeoutException("db timed out"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().message()).isEqualTo("Database is currently unavailable. Please try again later.");
    }

    @Test
    void shouldReturnServiceUnavailableForAppointmentEventPublishingException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppointmentEventPublishing(
                new AppointmentEventPublishingException("boom", new RuntimeException("kafka down")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().message()).isEqualTo("Messaging service is currently unavailable. Please try again later.");
    }

    @Test
    void shouldReturnBadRequestForConstraintViolation() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("scheduledAt");
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be in the future");

        ResponseEntity<ApiResponse<Map<String, String>>> response =
                handler.handleConstraintViolation(new ConstraintViolationException(Set.of(violation)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).containsEntry("scheduledAt", "must be in the future");
    }

    @Test
    void shouldReturnBadRequestForMethodArgumentTypeMismatch() {
        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("string", UUID.class, "id", parameter, null);

        ResponseEntity<ApiResponse<Void>> response = handler.handleMethodArgumentTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid value for parameter: id");
    }

    @Test
    void shouldReturnBadRequestForHttpMessageNotReadable() {
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("Cannot deserialize value of type `Status`", inputMessage);

        ResponseEntity<ApiResponse<Void>> response = handler.handleHttpMessageNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().message()).isEqualTo("Malformed or invalid request body.");
    }

    @Test
    void shouldReturnNotFoundForUnmappedStaticResource() {
        ResponseEntity<Void> response = handler.handleNoResourceFound(new NoResourceFoundException(HttpMethod.GET, "/unknown", "/unknown"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Unexpected error. Please try again later.");
    }
}
