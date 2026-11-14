package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.enumeration.TargetLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateFlashcardsWithAIDto(
        @NotNull
        Long collectionId,
        @NotBlank
        @Size(min = 10, max = 3000, message = "textToScan must not exceed 3000 characters")
        String textToScan,
        @NotNull
        TargetLanguage targetLanguage
) {
}
