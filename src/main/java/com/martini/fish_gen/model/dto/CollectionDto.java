package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.FlashcardCollection;

import java.time.LocalDateTime;

public record CollectionDto(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CollectionDto from(FlashcardCollection collection) {
        return new CollectionDto(
                collection.getId(),
                collection.getName(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
