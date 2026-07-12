package com.appointment.adapters.in.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentSortSanitizerTest {

    @Test
    void shouldFallBackToDefaultSortWhenPropertyIsNotSortable() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("string"));

        Pageable sanitized = AppointmentSortSanitizer.sanitize(pageable);

        assertThat(sanitized.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "scheduledAt"));
    }

    @Test
    void shouldKeepValidSortProperty() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "patientName"));

        Pageable sanitized = AppointmentSortSanitizer.sanitize(pageable);

        assertThat(sanitized.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "patientName"));
    }

    @Test
    void shouldDropOnlyInvalidPropertiesAndKeepValidOnesWhenMixed() {
        Sort mixedSort = Sort.by(Sort.Order.asc("status")).and(Sort.by(Sort.Order.desc("string")));
        Pageable pageable = PageRequest.of(0, 10, mixedSort);

        Pageable sanitized = AppointmentSortSanitizer.sanitize(pageable);

        assertThat(sanitized.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "status"));
    }

    @Test
    void shouldPreservePageNumberAndSize() {
        Pageable pageable = PageRequest.of(2, 5, Sort.by("string"));

        Pageable sanitized = AppointmentSortSanitizer.sanitize(pageable);

        assertThat(sanitized.getPageNumber()).isEqualTo(2);
        assertThat(sanitized.getPageSize()).isEqualTo(5);
    }
}
