package com.martini.fish_gen.model.dto;

public record CreateFishesFromPromptDto(
        String text,
        TargetLanguage targetLanguage
) {
}
