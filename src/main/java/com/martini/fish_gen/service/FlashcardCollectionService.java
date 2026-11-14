package com.martini.fish_gen.service;

import com.martini.fish_gen.mapper.FlashcardMapper;
import com.martini.fish_gen.model.dao.FlashcardCollection;
import com.martini.fish_gen.model.dto.GeneratedFlashcard;
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
    private final FlashcardMapper flashcardMapper;

    // Runs in its own transaction so the lazy flashcards collection can be loaded and the new flashcards saved
    @Transactional
    public void addGeneratedFlashcards(Long collectionId, List<GeneratedFlashcard> generatedFlashcards) {
        FlashcardCollection flashcardCollection = flashcardCollectionRepository.findById(collectionId)
                .orElseThrow(EntityNotFoundException::new);
        generatedFlashcards.stream()
                .map(flashcardMapper::toFlashcard)
                .forEach(flashcardCollection::addFlashcard);
    }
}
