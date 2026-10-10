package com.martini.fish_gen.model.dto;

public record TokenDto(String accessToken, String tokenType, long expiresIn) {
}
