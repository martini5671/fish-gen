package com.martini.fish_gen.event;

import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.service.FlashcardCollectionService;
import com.martini.fish_gen.service.FlashcardGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlashcardGenerationListener {

    private final FlashcardGeneratorService flashcardGeneratorService;
    private final FlashcardCollectionService flashcardCollectionService;

    // AFTER_COMMIT: nothing is generated if the request's transaction rolls back
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGenerationRequested(FlashcardGenerationRequestedEvent event) {
        try {
            List<GeneratedFlashcard> generatedFlashcards = flashcardGeneratorService.generateFlashcards(
                    event.textToScan(),
                    event.targetLanguage(),
                    event.username());
            flashcardCollectionService.addGeneratedFlashcards(event.collectionId(), generatedFlashcards);
            log.info("Flashcard generation request has been completed successfully for" +
                    " flashcard collection with id: {}", event.collectionId());
        } catch (RuntimeException e) {
            // Nobody waits on this thread, so the failure is only logged until generation requests get their own queue
            log.error("Flashcard generation failed for collection {}", event.collectionId(), e);
        }
    }
}
