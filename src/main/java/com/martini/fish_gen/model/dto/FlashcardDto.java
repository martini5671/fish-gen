package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotNull;

// source and target are nullable columns in the database, so they stay optional in the OpenAPI spec
public record FlashcardDto(
        @NotNull Long id,
        String source,
        String target
) {
}
