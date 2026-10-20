package com.martini.fish_gen.event;

import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import com.martini.fish_gen.service.FlashcardGeneratorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlashcardGenerationListener {

    private final FlashcardGeneratorService flashcardGeneratorService;
    private final FlashcardCollectionRepository flashcardCollectionRepository;

    // AFTER_COMMIT: the PENDING collection must be committed before another thread looks it up,
    // and nothing is generated if the request's transaction rolls back
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGenerationRequested(FlashcardGenerationRequestedEvent event) {
        try {
            FlashcardCollection flashcardCollection = flashcardCollectionRepository.findById(event.collectionId())
                    .orElseThrow(EntityNotFoundException::new);
            flashcardGeneratorService.generateFlashcards(event.textToScan(), event.targetLanguage())
                    .stream()
                    .map(generatedFlashcard -> Flashcard.builder()
                            .source(generatedFlashcard.source())
                            .target(generatedFlashcard.target())
                            .build())
                    .forEach(flashcardCollection::addFlashcard);
            flashcardCollection.setGenerationStatus(GenerationStatus.COMPLETED);
            log.info("Flashcard generation request has been completed successfully for" +
                    " flashcard collection with id: {}", event.collectionId());
            flashcardCollectionRepository.save(flashcardCollection);
        } catch (RuntimeException e) {
            // Nobody waits on this thread, so the failure is recorded on the collection for the client to see
            log.error("Flashcard generation failed for collection {}", event.collectionId(), e);
            flashcardCollectionRepository.findById(event.collectionId())
                    .ifPresent(collection -> {
                        collection.setGenerationStatus(GenerationStatus.FAILED);
                        flashcardCollectionRepository.save(collection);
                    });
        }
    }
}
