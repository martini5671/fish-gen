package com.martini.fish_gen.event;

import com.martini.fish_gen.model.enumeration.TargetLanguage;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

// Published once a PENDING collection is saved; carries everything generation needs, because the listener runs on
// another thread without the request's SecurityContext
@Builder
public record FlashcardGenerationRequestedEvent(
        @NotNull
        Long collectionId,
        @NotNull
        String textToScan,
        @NotNull
        TargetLanguage targetLanguage
) {
}
