package com.martini.fish_gen.model.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

// Both bounds are inclusive, e.g. createdFrom=2026-09-27T08:00:00&createdTo=2026-09-30T23:59:59
public record CollectionFilter(
        String name,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdTo
) {
}
