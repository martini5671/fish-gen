package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.Flashcard;
import jakarta.validation.constraints.NotNull;

// source and target are nullable columns in the database, so they stay optional in the OpenAPI spec
public record FlashcardDto(
        @NotNull Long id,
        String source,
        String target
) {
    public static FlashcardDto from(Flashcard flashcard) {
        return new FlashcardDto(
                flashcard.getId(),
                flashcard.getSource(),
                flashcard.getTarget()
        );
    }
}
