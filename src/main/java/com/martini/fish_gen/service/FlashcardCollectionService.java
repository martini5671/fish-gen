package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dao.Flashcard;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
import com.martini.fish_gen.model.enumeration.GenerationStatus;
import com.martini.fish_gen.repository.FlashcardCollectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardCollectionService {

    private final FlashcardCollectionRepository flashcardCollectionRepository;

    // Runs in its own transaction so the lazy flashcards collection can be loaded and the status is saved
    // together with the flashcards
    @Transactional
    public void completeGeneration(Long collectionId, List<GeneratedFlashcard> generatedFlashcards) {
        FlashcardCollection flashcardCollection = flashcardCollectionRepository.findById(collectionId)
                .orElseThrow(EntityNotFoundException::new);
        generatedFlashcards.stream()
                .map(generatedFlashcard -> Flashcard.builder()
                        .source(generatedFlashcard.source())
                        .target(generatedFlashcard.target())
                        .build())
                .forEach(flashcardCollection::addFlashcard);
        flashcardCollection.setGenerationStatus(GenerationStatus.COMPLETED);
    }

    @Transactional
    public void setGenerationStatus(Long collectionId, GenerationStatus status) {
        flashcardCollectionRepository.findById(collectionId)
                .ifPresent(flashcardCollection -> {
                    flashcardCollection.setGenerationStatus(status);
                    flashcardCollectionRepository.save(flashcardCollection);
                });
    }
}
