package com.appointment.adapters.in.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Falls back to the default sort when a requested sort property isn't a real field (e.g. Swagger
 * UI's unedited "string" placeholder for the sort array), instead of letting an invalid Sort
 * expression blow up the query.
 */
final class AppointmentSortSanitizer {

    private static final Set<String> SORTABLE_PROPERTIES =
            Set.of("scheduledAt", "status", "patientName", "createdAt", "updatedAt");

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "scheduledAt");

    private AppointmentSortSanitizer() {
    }

    static Pageable sanitize(Pageable pageable) {
        Sort validOrders = Sort.by(pageable.getSort().stream()
                .filter(order -> SORTABLE_PROPERTIES.contains(order.getProperty()))
                .toList());

        Sort sort = validOrders.isSorted() ? validOrders : DEFAULT_SORT;
        return pageable.getSort().equals(sort)
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
