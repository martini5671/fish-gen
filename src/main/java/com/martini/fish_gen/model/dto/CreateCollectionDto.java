package com.martini.fish_gen.model.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Builder;

// A null or blank name means the backend generates one
@Builder
public record CreateCollectionDto(@Nullable @Size(max = 255) String name) {
}
