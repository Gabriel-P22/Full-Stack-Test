package com.desafio.agendamento.adapters.in.controller.dtos;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        T data,
        String message,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> of(T data, String message) {
        return new ApiResponse<>(data, message, LocalDateTime.now());
    }
}
