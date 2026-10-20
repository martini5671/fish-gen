package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.model.enumeration.TargetLanguage;

import java.time.LocalDateTime;

public record CollectionDto(
        Long id,
        String name,
        TargetLanguage targetLanguage,
        GenerationStatus generationStatus,
        LocalDateTime createdAt,
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
