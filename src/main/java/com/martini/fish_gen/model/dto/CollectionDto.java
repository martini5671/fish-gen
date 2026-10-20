package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.model.enumeration.TargetLanguage;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// @NotNull marks fields as required in the OpenAPI spec; the rest are nullable columns in the database
public record CollectionDto(
        @NotNull Long id,
        @NotNull String name,
        TargetLanguage targetLanguage,
        @NotNull GenerationStatus generationStatus,
        @NotNull LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CollectionDto from(FlashcardCollection collection) {
        return new CollectionDto(
                collection.getId(),
                collection.getName(),
                collection.getTargetLanguage(),
                collection.getGenerationStatus(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
