package com.desafio.agendamento.frameworks.exceptions;

import com.desafio.agendamento.adapters.in.controller.dtos.ApiResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
    void shouldReturnConflictForAppointmentAlreadyCanceled() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAppointmentAlreadyCanceled(new AppointmentAlreadyCanceledException("already canceled"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("already canceled");
    }

    @Test
    void shouldReturnBadRequestForCancellationObservationRequired() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppointmentCancellationObservationRequired(
                new AppointmentCancellationObservationRequiredException("observation is required"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("observation is required");
    }

    @Test
    void shouldReturnNotFoundForAppointmentNotFound() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAppointmentNotFound(new AppointmentNotFoundException("not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("not found");
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
