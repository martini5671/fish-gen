package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginDto(@NotBlank String username,
                       @NotBlank String password) {
}
