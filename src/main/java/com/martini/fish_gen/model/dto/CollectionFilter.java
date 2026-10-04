package com.martini.fish_gen.model.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CollectionFilter(
        String name,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdTo
) {
}
