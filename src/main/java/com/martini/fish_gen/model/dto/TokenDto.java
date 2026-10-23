package com.martini.fish_gen.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @param expiresAt epoch milliseconds, directly comparable with JavaScript's {@code Date.now()}
 */
public record TokenDto(@NotNull String accessToken,
                       @NotNull long expiresAt,
                       @NotNull String username) {
}
