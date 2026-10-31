package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FlashcardCollectionStatusDto(
        @NotNull
        Long collectionId,
        @NotNull
        GenerationStatus generationStatus
) {
        public static FlashcardCollectionStatusDto from(FlashcardCollection flashcardCollection) {
                return FlashcardCollectionStatusDto.builder()
                        .collectionId(flashcardCollection.getId())
                        .generationStatus(flashcardCollection.getGenerationStatus())
                        .build();
        }
}
