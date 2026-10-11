package com.martini.fish_gen.model.dto;

import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.enumeration.TargetLanguage;

public record FlashcardDto(
        Long id,
        String source,
        String target,
        TargetLanguage targetLanguage
) {
    public static FlashcardDto from(Flashcard flashcard) {
        return new FlashcardDto(
                flashcard.getId(),
                flashcard.getSource(),
                flashcard.getTarget(),
                flashcard.getTargetLanguage()
        );
    }
}
