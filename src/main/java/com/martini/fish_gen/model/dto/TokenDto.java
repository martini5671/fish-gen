package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotNull;

public record TokenDto(@NotNull String accessToken,
                       @NotNull String tokenType,
                       @NotNull long expiresIn) {
}
