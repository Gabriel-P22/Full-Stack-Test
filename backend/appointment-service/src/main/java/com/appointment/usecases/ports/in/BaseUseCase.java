package com.appointment.usecases.ports.in;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BaseUseCase<T> {
    public T execute(@Valid T entity);
}
