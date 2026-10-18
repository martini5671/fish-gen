package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.Flashcard;

public record FlashcardDto(
        Long id,
        String source,
        String target
) {
    public static FlashcardDto from(Flashcard flashcard) {
        return new FlashcardDto(
                flashcard.getId(),
                flashcard.getSource(),
                flashcard.getTarget()
        );
    }
}
