package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// @NotNull marks fields as required in the OpenAPI spec; the rest are nullable columns in the database
public record CollectionDto(
        @NotNull Long id,
        @NotNull String name,
        @NotNull LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
