package com.martini.fish_gen.exception;

public class FlashCardGenerationException extends RuntimeException {
    public FlashCardGenerationException(String failedToGenerateFlashcards, RuntimeException e) {
        super(failedToGenerateFlashcards, e);
    }

    public FlashCardGenerationException(String failedToGenerateFlashcards) {
        super(failedToGenerateFlashcards);
    }
}
