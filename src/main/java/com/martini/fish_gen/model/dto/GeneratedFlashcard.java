package com.martini.fish_gen.model.dto;

import lombok.Builder;

@Builder
public record GeneratedFlashcard(
        String source,
        String target
) {
}
