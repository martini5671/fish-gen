package com.martini.fish_gen.model.dto;

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
}
