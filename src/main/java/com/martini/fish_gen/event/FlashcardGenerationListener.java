package com.martini.fish_gen.event;

import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
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

    // AFTER_COMMIT: the PENDING collection must be committed before another thread looks it up,
    // and nothing is generated if the request's transaction rolls back.
    // Not transactional itself, so a failed generation can't roll back the FAILED status update
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGenerationRequested(FlashcardGenerationRequestedEvent event) {
        try {
            List<GeneratedFlashcard> generatedFlashcards = flashcardGeneratorService.generateFlashcards(
                    event.textToScan(),
                    event.targetLanguage(),
                    event.username());
            flashcardCollectionService.completeGeneration(event.collectionId(), generatedFlashcards);
            log.info("Flashcard generation request has been completed successfully for" +
                    " flashcard collection with id: {}", event.collectionId());
        } catch (RuntimeException e) {
            // Nobody waits on this thread, so the failure is recorded on the collection for the client to see
            log.error("Flashcard generation failed for collection {}", event.collectionId(), e);
            flashcardCollectionService.setGenerationStatus(event.collectionId(), GenerationStatus.FAILED);
        }
    }
}
