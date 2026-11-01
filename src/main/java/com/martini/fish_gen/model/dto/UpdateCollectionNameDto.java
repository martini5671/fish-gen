package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

// Same limit as the name column and CreateFlashcardsDto
@Builder
public record UpdateCollectionNameDto(@NotBlank @Size(max = 255) String collectionName) {
}
